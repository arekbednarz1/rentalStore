import { LitElement, html, css } from 'lit';
import { customElement, state } from 'lit/decorators.js';

interface UserDto {
    name: string;
    email: string;
    role: string;
}

@customElement('user-list')
export class UserList extends LitElement {
    static styles = css`
        section {
            max-width: 720px;
            margin: 2rem auto;
            padding: 1.5rem;
            background: #fefefe;
            border-radius: 8px;
            box-shadow: 0 0 12px rgba(0, 0, 0, 0.05);
        }
        h2 {
            margin-bottom: 1rem;
            color: #2e7d32;
        }

        ul {
            list-style: none;
            padding: 0;
            margin: 0;
        }

        li.user-card {
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: #f4f4f4;
            margin-bottom: 0.7rem;
            padding: 0.8rem 1rem;
            border-radius: 6px;
            font-size: 14px;
        }

        .user-info strong {
            color: #2e7d32;
        }

        lion-button {
            --button-background-color: #c62828;
            --button-color: white;
            font-size: 13px;
            padding: 0.4rem 0.8rem;
            border-radius: 4px;
        }

        .message {
            margin-top: 1.2rem;
            text-align: center;
            font-weight: bold;
        }

        .error {
            color: #c62828;
        }

        .success {
            color: #2e7d32;
        }
    `;

    @state() private users: UserDto[] = [];
    @state() private message = '';
    @state() private isError = false;
    @state() private loading = true;

    async connectedCallback() {
        super.connectedCallback();
        const token = localStorage.getItem('accessToken');

        if (!token) {
            window.location.href = '/login';
            return;
        }

        try {
            const res = await fetch('/api/v1/users/list', {
                headers: { Authorization: `Bearer ${token}` }
            });

            if (res.status === 403) {
                window.location.href = '/login';
                return;
            }

            if (!res.ok) {
                this.message = `Could not fetch users. Status: ${res.status}`;
                this.isError = true;
                return;
            }

            const data: unknown = await res.json();
            if (!Array.isArray(data)) {
                this.message = 'Invalid user list format.';
                this.isError = true;
                return;
            }

            this.users = data as UserDto[];
        } catch (err: any) {
            this.message = `Error fetching users: ${err.message}`;
            this.isError = true;
        } finally {
            this.loading = false;
        }
    }

    private async deleteUser(email: string) {
        const token = localStorage.getItem('accessToken');
        if (!token) return;

        this.message = '';
        this.isError = false;

        try {
            const res = await fetch(`/api/v1/users?email=${encodeURIComponent(email)}`, {
                method: 'DELETE',
                headers: {
                    Authorization: `Bearer ${token}`
                },
            });

            if (res.status === 400) {
                this.message = `Cannot delete yourself.`;
                this.isError = true;
                return;
            }

            if (!res.ok) {
                this.message = `Delete failed (status ${res.status})`;
                this.isError = true;
                return;
            }

            this.users = this.users.filter(user => user.email !== email);
            this.message = `User ${email} deleted successfully.`;
            this.isError = false;
        } catch (err: any) {
            this.message = `Error deleting user: ${err.message}`;
            this.isError = true;
        }
    }

    render() {
        return html`
            <section>
                <h2>User List</h2>

                ${this.loading
                        ? html`<p>Loading...</p>`
                        : html`
                            <ul>
                                ${this.users.length === 0
                                ? html`<li>No users found.</li>`
                                : this.users.map(user => html`
                                            <li class="user-card">
                                                <div class="user-info">
                                                    <strong>${user.name}</strong> — ${user.email} [${user.role}]
                                                </div>
                                                <lion-button
                                                        @click=${() => this.deleteUser(user.email)}
                                                        aria-label="Delete user"
                                                >
                                                    Delete
                                                </lion-button>
                                            </li>
                                        `)}
                            </ul>
                        `
                }

                ${this.message && html`
                    <div class="message ${this.isError ? 'error' : 'success'}">
                        ${this.message}
                    </div>
                `}
            </section>
        `;
    }
}
