import { html, css, LitElement } from 'lit';
import { customElement, state } from 'lit/decorators.js';

@customElement('register-form')
export class RegisterForm extends LitElement {
    static styles = css`
        form {
            display: flex;
            flex-direction: column;
            gap: 16px;
            max-width: 400px;
            margin: 2rem auto;
            background: #f8f8f8;
            padding: 2rem;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }
        input {
            padding: 0.5rem;
            font-size: 14px;
            border: 1px solid #ccc;
            border-radius: 4px;
        }
        button {
            padding: 0.7rem;
            background: #2e7d32;
            color: white;
            font-weight: bold;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        button[disabled] {
            background: #9e9e9e;
            cursor: not-allowed;
        }
        button:hover:not([disabled]) {
            background: #1b5e20;
        }
        .message {
            text-align: center;
            margin-top: 1rem;
            font-weight: bold;
        }
        .error {
            color: #c62828;
        }
        .success {
            color: #2e7d32;
        }
    `;

    @state() private username = '';
    @state() private email = '';
    @state() private password = '';
    @state() private responseMessage = '';
    @state() private isError = false;
    @state() private isSubmitting = false;

    private async handleSubmit(e: Event) {
        e.preventDefault();
        this.responseMessage = '';
        this.isError = false;
        this.isSubmitting = true;

        const body = {
            username: this.username.trim(),
            email: this.email.trim(),
            password: this.password,
        };
        try {
            const res = await fetch('/api/v1/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body),
            });

            if (res.ok) {
                const data = await res.json();
                localStorage.setItem('accessToken', data.accessToken);
                localStorage.setItem('refreshToken', data.refreshToken);

                this.responseMessage = `Registered!`;
                this.isError = false;
                this.username = '';
                this.email = '';
                this.password = '';
                window.location.href = '/';
            }
            else {
                const err = await res.text();
                this.responseMessage = `Error: ${err}`;
                this.isError = true;
            }
        } catch (error: any) {
            this.responseMessage = `Connection error: ${error.message}`;
            this.isError = true;
        } finally {
            this.isSubmitting = false;
        }
    }

    render() {
        return html`
            <form @submit=${this.handleSubmit}>
                <h2>Registration</h2>

                <input
                        type="text"
                        placeholder="User name"
                        .value=${this.username}
                        @input=${(e: InputEvent) => this.username = (e.target as HTMLInputElement).value}
                        required
                />
                <input
                        type="email"
                        placeholder="Email"
                        .value=${this.email}
                        @input=${(e: InputEvent) => this.email = (e.target as HTMLInputElement).value}
                        required
                />
                <input
                        type="password"
                        placeholder="Password"
                        .value=${this.password}
                        @input=${(e: InputEvent) => this.password = (e.target as HTMLInputElement).value}
                        required
                />
                <button type="submit" ?disabled=${this.isSubmitting}>
                    ${this.isSubmitting ? 'Sending...' : 'Register'}
                </button>
                <div style="text-align: center; margin-top: 1rem;">
                    <a href="/" style="color: #2e7d32; text-decoration: underline; font-size: 14px;">
                        If you have an account login
                    </a>
                </div>
                <div class="message ${this.isError ? 'error' : 'success'}">
                    ${this.responseMessage}
                </div>
            </form>
        `;
    }
}