package co.lecompany.app.views.bookslist;

import co.lecompany.app.data.entity.SampleBook;
import co.lecompany.app.data.service.SampleBookService;
import co.lecompany.app.views.MainLayout;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@PageTitle("Books List")
@Route(value = "books-list/:sampleBookID?/:action?(edit)", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@Tag("books-list-view")
@JsModule("./views/bookslist/books-list-view.ts")
public class BooksListView extends LitTemplate implements HasStyle, BeforeEnterObserver {

    private final String SAMPLEBOOK_ID = "sampleBookID";
    private final String SAMPLEBOOK_EDIT_ROUTE_TEMPLATE = "books-list/%s/edit";

    // This is the Java companion file of a design
    // You can find the design file inside /frontend/views/
    // The design can be easily edited by using Vaadin Designer
    // (vaadin.com/designer)

    @Id
    private Grid<SampleBook> grid;

    @Id
    private Upload image;
    @Id
    private Image imagePreview;
    @Id
    private TextField name;
    @Id
    private TextField author;
    @Id
    private DatePicker publicationDate;
    @Id
    private TextField pages;
    @Id
    private TextField isbn;

    @Id
    private Button cancel;
    @Id
    private Button save;

    private BeanValidationBinder<SampleBook> binder;

    private SampleBook sampleBook;

    private final SampleBookService sampleBookService;

    @Autowired
    public BooksListView(SampleBookService sampleBookService) {
        this.sampleBookService = sampleBookService;
        addClassNames("books-list-view");
        LitRenderer<SampleBook> imageRenderer = LitRenderer
                .<SampleBook>of("<img style='height: 64px' src=${item.image} />").withProperty("image", item -> {
                    if (item != null && item.getImage() != null) {
                        return "data:image;base64," + Base64.getEncoder().encodeToString(item.getImage());
                    } else {
                        return "";
                    }
                });
        grid.addColumn(imageRenderer).setHeader("Image").setWidth("68px").setFlexGrow(0);

        grid.addColumn(SampleBook::getName).setHeader("Name").setAutoWidth(true);
        grid.addColumn(SampleBook::getAuthor).setHeader("Author").setAutoWidth(true);
        grid.addColumn(SampleBook::getPublicationDate).setHeader("Publication Date").setAutoWidth(true);
        grid.addColumn(SampleBook::getPages).setHeader("Pages").setAutoWidth(true);
        grid.addColumn(SampleBook::getIsbn).setHeader("Isbn").setAutoWidth(true);
        grid.setItems(query -> sampleBookService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(SAMPLEBOOK_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(BooksListView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(SampleBook.class);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(pages).withConverter(new StringToIntegerConverter("Only numbers are allowed")).bind("pages");

        binder.bindInstanceFields(this);

        attachImageUpload(image, imagePreview);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.sampleBook == null) {
                    this.sampleBook = new SampleBook();
                }
                binder.writeBean(this.sampleBook);
                sampleBookService.update(this.sampleBook);
                clearForm();
                refreshGrid();
                Notification.show("SampleBook details stored.");
                UI.getCurrent().navigate(BooksListView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the sampleBook details.");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<UUID> sampleBookId = event.getRouteParameters().get(SAMPLEBOOK_ID).map(UUID::fromString);
        if (sampleBookId.isPresent()) {
            Optional<SampleBook> sampleBookFromBackend = sampleBookService.get(sampleBookId.get());
            if (sampleBookFromBackend.isPresent()) {
                populateForm(sampleBookFromBackend.get());
            } else {
                Notification.show(String.format("The requested sampleBook was not found, ID = %s", sampleBookId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(BooksListView.class);
            }
        }
    }

    private void attachImageUpload(Upload upload, Image preview) {
        ByteArrayOutputStream uploadBuffer = new ByteArrayOutputStream();
        upload.setAcceptedFileTypes("image/*");
        upload.setReceiver((fileName, mimeType) -> {
            uploadBuffer.reset();
            return uploadBuffer;
        });
        upload.addSucceededListener(e -> {
            StreamResource resource = new StreamResource(e.getFileName(),
                    () -> new ByteArrayInputStream(uploadBuffer.toByteArray()));
            preview.setSrc(resource);
            preview.setVisible(true);
            if (this.sampleBook == null) {
                this.sampleBook = new SampleBook();
            }
            this.sampleBook.setImage(uploadBuffer.toByteArray());
        });
        preview.setVisible(false);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getLazyDataView().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(SampleBook value) {
        this.sampleBook = value;
        binder.readBean(this.sampleBook);
        this.imagePreview.setVisible(value != null);
        if (value == null || value.getImage() == null) {
            this.image.clearFileList();
            this.imagePreview.setSrc("");
        } else {
            this.imagePreview.setSrc("data:image;base64," + Base64.getEncoder().encodeToString(value.getImage()));
        }

    }
}
