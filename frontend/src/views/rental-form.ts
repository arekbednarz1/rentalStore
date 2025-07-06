import { LitElement, html, css } from 'lit';
import { customElement, state, property } from 'lit/decorators.js';
import '@lion/form/define';
import '@lion/select/define';
import '@lion/button/define';

@customElement('rental-form')
export class RentalForm extends LitElement {
    static styles = css`
        .form-wrapper {
            padding: 1.5rem;
            background: #f9f9f9;
            border-radius: 8px;
            max-width: 400px;
            margin: 2rem auto;
        }

        lion-select {
            margin-bottom: 1rem;
        }

        lion-button {
            --button-background-color: #2e7d32;
            --button-color: white;
            cursor: pointer;
        }

        .message {
            margin-top: 1rem;
            text-align: center;
            font-weight: bold;
        }

        .error {
            color: #c62828;
        }

        .success {
            color: #2e7d32;
        }

        .disabled {
            opacity: 0.6;
            pointer-events: none;
        }
    `;

    @property({ type: Number }) movieId!: number;
    @state() private rentTime: string = 'ONE_DAY';
    @state() private message = '';
    @state() private isError = false;
    @state() private isSubmitting = false;

    private async rentMovie() {
        if (!this.movieId || !this.rentTime || this.isSubmitting) return;
        const token = localStorage.getItem('accessToken');
        if (!token) {
            this.message = `You must be logged in to rent a movie`;
            this.isError = true;
            return;
        }

        this.isSubmitting = true;
        this.message = '';

        try {
            const res = await fetch(`/api/v1/rental/${this.movieId}/rent?dueDate=${this.rentTime}`, {
                method: 'PUT',
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!res.ok) {
                this.message = `Rental failed. Status: ${res.status}`;
                this.isError = true;
                return;
            }
            this.message = `Movie rented for ${this.rentTime.replace('_', ' ').toLowerCase()}`;
            this.isError = false;
            window.location.href = '/movies';
        } catch (err: any) {
            this.message = `Error renting movie: ${err.message}`;
            this.isError = true;
        } finally {
            this.isSubmitting = false;
        }
    }


    render() {
        return html`
            <div class="form-wrapper">
                ${typeof this.movieId !== 'number' || isNaN(this.movieId)
                        ? html`<div class="message error">Movie ID not provided.</div>`
                        : html`
              <lion-form>
                <lion-select
                  name="dueDate"
                  label="Choose rental time"
                  .modelValue=${this.rentTime}
                  @model-value-changed=${(e: CustomEvent) =>
                                this.rentTime = e.detail?.element?.modelValue ?? 'ONE_DAY'}>
                  <select slot="input">
                    <option value="ONE_DAY">1 Day</option>
                    <option value="ONE_WEEK">1 Week</option>
                  </select>
                </lion-select>

                <lion-button
                  class=${this.isSubmitting ? 'disabled' : ''}
                  @click=${this.rentMovie}>
                  Rent Now
                </lion-button>
              </lion-form>
            `
                }

                ${this.message && html`
                    <div class="message ${this.isError ? 'error' : 'success'}">
                        ${this.message}
                    </div>
                `}
            </div>
        `;
    }
}
