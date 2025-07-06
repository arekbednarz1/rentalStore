import { html, LitElement } from 'lit';
import { customElement } from 'lit/decorators.js';
import './rental-form';

@customElement('rental-view')
export class RentalView extends LitElement {
    render() {
        const params = new URLSearchParams(location.search);
        const id = Number(params.get('movieId'));
        return html`
      <rental-form .movieId=${id}></rental-form>`;
    }
}
