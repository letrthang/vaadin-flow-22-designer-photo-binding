import '@vaadin/vaadin-button';
import '@vaadin/vaadin-date-picker';
import '@vaadin/vaadin-date-time-picker';
import '@vaadin/vaadin-form-layout';
import '@vaadin/vaadin-grid';
import '@vaadin/vaadin-grid/vaadin-grid-column';
import '@vaadin/vaadin-ordered-layout/vaadin-horizontal-layout';
import '@vaadin/vaadin-split-layout';
import '@vaadin/vaadin-text-field';
import { html, LitElement } from 'lit';
import { customElement } from 'lit/decorators.js';

@customElement('books-list-view')
export class BooksListView extends LitElement {
  createRenderRoot() {
    // Do not use a shadow root
    return this;
  }

  render() {
    return html`<vaadin-split-layout>
      <div class="grid-wrapper">
        <vaadin-grid id="grid"></vaadin-grid>
      </div>
      <div class="editor-layout">
        <div class="editor">
          <vaadin-form-layout>
            <label>Image</label>
            <vaadin-upload accept="image/*" max-files="1" style="box-sizing: border-box" id="image"
              ><img id="imagePreview" class="w-full" /> </vaadin-upload
            ><vaadin-text-field label="Name" id="name"></vaadin-text-field
            ><vaadin-text-field label="Author" id="author"></vaadin-text-field
            ><vaadin-date-picker label="Publication date" id="publicationDate"></vaadin-date-picker
            ><vaadin-text-field label="Pages" id="pages"></vaadin-text-field
            ><vaadin-text-field label="Isbn" id="isbn"></vaadin-text-field>
          </vaadin-form-layout>
        </div>
        <vaadin-horizontal-layout class="button-layout">
          <vaadin-button theme="primary" id="save">Save</vaadin-button>
          <vaadin-button theme="tertiary" slot="" id="cancel">Cancel</vaadin-button>
        </vaadin-horizontal-layout>
      </div>
    </vaadin-split-layout>`;
  }
}
