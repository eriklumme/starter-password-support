import { showNotification } from '@vaadin/flow-frontend/a-notification';
import { EndpointError } from '@vaadin/flow-frontend/Connect';
import { CSSModule } from '@vaadin/flow-frontend/css-utils';
import { Binder, field } from '@vaadin/form';
import '@vaadin/vaadin-button/vaadin-button';
import '@vaadin/vaadin-custom-field';
import '@vaadin/vaadin-date-picker';
import '@vaadin/vaadin-form-layout/vaadin-form-layout';
import '@vaadin/vaadin-grid';
import { GridDataProviderCallback, GridDataProviderParams } from '@vaadin/vaadin-grid/vaadin-grid';
import '@vaadin/vaadin-grid/vaadin-grid-sort-column';
import '@vaadin/vaadin-ordered-layout/vaadin-horizontal-layout';
import '@vaadin/vaadin-split-layout/vaadin-split-layout';
import '@vaadin/vaadin-text-field';
import '@vaadin/vaadin-text-field/vaadin-number-field';
import '@vaadin/vaadin-text-field/vaadin-password-field';
import '@vaadin/vaadin-upload';
import { UploadElement, UploadFile } from '@vaadin/vaadin-upload';
import { customElement, html, LitElement, property, query, unsafeCSS } from 'lit-element';
import User from '../../generated/com/example/application/data/entity/User';
import UserModel from '../../generated/com/example/application/data/entity/UserModel';
import * as UserEndpoint from '../../generated/UserEndpoint';
import styles from './masterdetailviewtypescript-view.css';

@customElement('masterdetailviewtypescript-view')
export class MasterdetailviewtypescriptView extends LitElement {
  static get styles() {
    return [CSSModule('lumo-typography'), unsafeCSS(styles)];
  }

  @query('#grid')
  private grid: any;

  @property({ type: Number })
  private gridSize = 0;

  private gridDataProvider = this.getGridData.bind(this);

  private binder = new Binder<any, any>(this, UserModel);

  render() {
    return html`
      <vaadin-split-layout class="full-size">
        <div class="grid-wrapper">
          <vaadin-grid
            id="grid"
            class="full-size"
            theme="no-border"
            .size="${this.gridSize}"
            .dataProvider="${this.gridDataProvider}"
            @active-item-changed=${this.itemSelected}
          >
            <vaadin-grid-column width="96px" flex-grow="0" path="profilePicture"
              ><template
                ><span
                  style="border-radius: 50%; overflow: hidden; display: flex; align-items: center; justify-content: center; width: 64px; height: 64px"
                  ><img style="max-width: 100%" src="[[item.profilePicture]]" /></span></template></vaadin-grid-column
            ><vaadin-grid-sort-column auto-width path="email"></vaadin-grid-sort-column>
          </vaadin-grid>
        </div>
        <div id="editor-layout">
          <div id="editor">
            <vaadin-form-layout
              ><label>Profile picture</label>
              <vaadin-upload
                accept="image/*"
                max-files="1"
                style="box-sizing: border-box"
                id="profilePicture"
                @upload-request="${(e: CustomEvent) => this.handleImageUpload(e, 'profilePicture')}"
                ><img
                  class="full-width"
                  ?hidden="${!this.binder.value.profilePicture}"
                  src="${this.binder.value.profilePicture}"
                /> </vaadin-upload
              ><vaadin-text-field label="Email" id="email" ...="${field(this.binder.model.email)}"></vaadin-text-field
              ><vaadin-password-field
                label="Password"
                helper-text="Leave empty to keep current password"
                id="password"
                ...="${field(this.binder.model.password)}"
              ></vaadin-password-field
            ></vaadin-form-layout>
          </div>
          <vaadin-horizontal-layout id="button-layout" theme="spacing">
            <vaadin-button theme="primary" @click="${this.save}">Save</vaadin-button>
            <vaadin-button theme="tertiary" @click="${this.cancel}">Cancel</vaadin-button>
          </vaadin-horizontal-layout>
        </div>
      </vaadin-split-layout>
    `;
  }

  private async getGridDataSize() {
    return UserEndpoint.count();
  }

  private async getGridData(params: GridDataProviderParams, callback: GridDataProviderCallback) {
    const index = params.page * params.pageSize;
    const data = await UserEndpoint.list(index, params.pageSize, params.sortOrders as any);
    callback(data);
  }

  // Wait until all elements in the template are ready to set their properties
  async firstUpdated(changedProperties: any) {
    super.firstUpdated(changedProperties);

    this.gridSize = await this.getGridDataSize();
  }

  private async itemSelected(event: CustomEvent) {
    const item: User = event.detail.value as User;
    this.grid.selectedItems = item ? [item] : [];

    if (item) {
      const personFromBackend = await UserEndpoint.get(item.id);
      personFromBackend ? this.binder.read(personFromBackend) : this.refreshGrid();
    } else {
      this.clearForm();
    }
  }

  private async handleImageUpload(e: CustomEvent, propertyName: string) {
    e.preventDefault();
    const upload: UploadElement = e.target as UploadElement;
    const file: UploadFile = e.detail.file;
    const reader = new FileReader();
    reader.addEventListener('load', (event) => {
      const result: string = event.target!.result as string;
      this.binder.value[propertyName] = result;
      this.requestUpdate('binder');
      upload.files = [];
    });
    reader.readAsDataURL(file);
  }

  private async save() {
    try {
      await this.binder.submitTo(UserEndpoint.update);

      if (!this.binder.value.id) {
        // We added a new item
        this.gridSize++;
      }
      this.clearForm();
      this.refreshGrid();
      showNotification('Person details stored.', { position: 'bottom-start' });
    } catch (error) {
      if (error instanceof EndpointError) {
        showNotification('Server error. ' + error.message, { position: 'bottom-start' });
      } else {
        throw error;
      }
    }
  }

  private cancel() {
    this.grid.activeItem = undefined;
  }

  private clearForm() {
    this.binder.clear();
  }

  private refreshGrid() {
    this.grid.selectedItems = [];
    this.grid.clearCache();
  }
}
