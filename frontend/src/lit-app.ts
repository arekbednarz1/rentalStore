import { LitElement, html, css } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import { Router } from '@lit-labs/router';

import './views/register-form.ts';
import './views/login-form.ts';
import './views/user-self.ts';
import './views/home-ui.ts';
import './views/movie-list.ts';
import './views/rental-view.ts';
import './views/user-details.ts';
import './views/reminder-panel.ts';

@customElement('lit-app')
export class LitApp extends LitElement {
    static styles = css`
        header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 1rem 2rem;
            background: #e8f5e9;
            border-bottom: 1px solid #ccc;
        }

        nav a {
            margin-right: 1rem;
            text-decoration: none;
            color: #2e7d32;
            font-weight: bold;
            cursor: pointer;
        }

        .logout {
            background: #c62828;
            color: white;
            border: none;
            padding: 6px 10px;
            border-radius: 4px;
            cursor: pointer;
        }

        .access-blocked {
            text-align: center;
            padding: 2rem;
            color: #c62828;
            font-weight: bold;
        }
    `;

    @state() private allowUserList = true;

    private async handleLogout() {
        const token = localStorage.getItem('accessToken');
        try {
            await fetch('/api/v1/auth/logout', {
                method: 'POST',
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });
        } catch (_) {
            // ignore logout failure
        } finally {
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            window.location.href = '/';
        }
    }

    private router = new Router(this, [
        { path: '/login', render: () => html`<login-form></login-form>` },
        { path: '/register', render: () => html`<register-form></register-form>` },
        { path: '/self', render: () => html`<user-self></user-self>` },
        { path: '/', render: () => html`<home-ui></home-ui>` },
        { path: '/movies', render: () => html`<movie-list></movie-list>` },
        { path: '/rent', render: () => html`<rental-view></rental-view>` },
        { path: '/details', render: () => html`<user-details></user-details>` },
        { path: '/reminder', render: () => html`<reminder-panel></reminder-panel>` }
    ]);

    render() {
        const isLoggedIn = localStorage.getItem('accessToken');
        return html`
            ${isLoggedIn
                    ? html`
                        <header>
                            <nav>
                                <a href="/">Home</a>
                                <a href="/self">Profile</a>
                                <a href="/movies">Movies</a>
                                <a href="/reminder">Reminder</a>
                            </nav>
                            <button class="logout" @click=${this.handleLogout}>Logout</button>
                        </header>
                    `
                    : null}

            ${this.router?.outlet()}
        `;
    }
}
