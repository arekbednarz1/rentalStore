import { html, css, LitElement } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import '@lion/button/define';

const formatter = new Intl.DateTimeFormat('pl-PL', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
});

interface UserDto {
    name: string;
    email: string;
    role: string;
}

interface RentalDto {
    movieTitle: string;
    rentedAt: string;
    dueDate: string;
}

@customElement('home-ui')
export class HomeUi extends LitElement {
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

        li {
            margin-bottom: 0.8rem;
            padding: 0.8rem 1rem;
            background: #f4f4f4;
            border-radius: 6px;
            font-size: 14px;
        }

        lion-button {
            margin-left: 0.5rem;
            --button-background-color: #c62828;
            --button-color: white;
            font-size: 13px;
            padding: 0.4rem 0.8rem;
            border-radius: 4px;
        }

        .error {
            color: #c62828;
            text-align: center;
            font-weight: bold;
            margin-top: 1rem;
        }

        .success {
            color: #2e7d32;
        }
    `;
    @state() private role = '';
    @state() private users: UserDto[] = [];
    @state() private rentals: RentalDto[] = [];
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
            const res = await fetch('/api/v1/users/self', {
                headers: { Authorization: `Bearer ${token}` }
            });

            const user = await res.json();
            this.role = user.role;

            if (this.role === 'ADMIN') {
                const userRes = await fetch('/api/v1/users/list', {
                    headers: { Authorization: `Bearer ${token}` }
                });
                this.users = await userRes.json();
            } else {
                const rentalRes = await fetch('/api/v1/rental/self/0/20/rentals?returned=false', {
                    headers: { Authorization: `Bearer ${token}` }
                });
                this.rentals = await rentalRes.json();
            }
        } catch (err: any) {
            this.message = `Error: ${err.message}`;
            this.isError = true;
        } finally {
            this.loading = false;
        }
    }

    private async deleteUser(email: string) {
        const token = localStorage.getItem('accessToken');
        if (!token) return;

        try {
            const res = await fetch(`/api/v1/users?email=${encodeURIComponent(email)}`, {
                method: 'DELETE',
                headers: { Authorization: `Bearer ${token}` }
            });

            if (!res.ok) {
                this.message = `Delete failed. Status: ${res.status}`;
                this.isError = true;
                return;
            }

            this.users = this.users.filter(user => user.email !== email);
            this.message = `User ${email} deleted.`;
            this.isError = false;
        } catch (err: any) {
            this.message = `Delete error: ${err.message}`;
            this.isError = true;
        }
    }

    private navigateToDetails(email: string) {
        window.location.href = `/details?email=${encodeURIComponent(email)}`;
    }

    render() {
        return html`
      <section>
        <h2>${this.role === 'ADMIN' ? 'User List' : 'Your Rentals'}</h2>

        ${this.loading
            ? html`<p>Loading...</p>`
            : this.role === 'ADMIN'
                ? html`
                <ul>
                  ${this.users.length === 0
                    ? html`<li>No users found.</li>`
                    : this.users.map(user => html`
                        <li>
                          <strong>${user.name}</strong> — ${user.email} [${user.role}]
                          <lion-button @click=${() => this.deleteUser(user.email)}>Delete</lion-button>
                          <lion-button @click=${() => this.navigateToDetails(user.email)}>Details</lion-button>
                        </li>
                      `)}
                </ul>
              `
                : html`
                <ul>
                  ${this.rentals.length === 0
                    ? html`<li>No active rentals.</li>`
                    : this.rentals.map(r => html`
                        <li>
                          <strong>${r.movieTitle}</strong><br />
                          Rented: ${formatter.format(new Date(r.rentedAt))}<br />
                          Due: ${formatter.format(new Date(r.dueDate))}
                        </li>
                      `)}
                </ul>
              `
        }

        ${this.message && html`
          <div class=${this.isError ? 'error' : 'success'}>${this.message}</div>
        `}
      </section>
    `;
    }
}
