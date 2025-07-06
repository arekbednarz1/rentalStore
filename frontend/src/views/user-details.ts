import { html, css, LitElement } from 'lit';
import { customElement, state } from 'lit/decorators.js';

const formatter = new Intl.DateTimeFormat('pl-PL', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
});

@customElement('user-details')
export class UserDetails extends LitElement {
    static styles = css`
        section {
            max-width: 460px;
            margin: 2rem auto;
            padding: 1rem 1.5rem;
            background: #fefefe;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.08);
        }

        h2, h3 {
            margin-top: 0;
            color: #2e7d32;
        }

        p {
            margin: 0.5rem 0;
            font-size: 15px;
        }

        input, button {
            display: block;
            margin-bottom: 1rem;
            width: 100%;
            padding: 0.6rem;
            font-size: 14px;
            border-radius: 4px;
            box-sizing: border-box;
        }

        input {
            border: 1px solid #ccc;
        }

        button {
            background-color: #2e7d32;
            color: white;
            font-weight: bold;
            border: none;
            cursor: pointer;
        }

        button:hover {
            background-color: #1b5e20;
        }

        dialog {
            border: none;
            border-radius: 8px;
            padding: 1.5rem;
            width: 100%;
            max-width: 440px;
            box-shadow: 0 0 18px rgba(0, 0, 0, 0.35);
            background: #ffffff;
        }

        .error, .success {
            font-weight: bold;
            padding: 0.4rem 0;
            text-align: center;
        }

        .error {
            color: #c62828;
        }

        .success {
            color: #2e7d32;
        }

        .rentals-card {
            background: #f9f9f9;
            padding: 1rem;
            border: 1px solid #ccc;
            border-radius: 6px;
            margin-bottom: 1rem;
        }

        .rental-controls {
            display: flex;
            justify-content: center;
            gap: 1rem;
            margin: 1rem 0;
        }

        .pagination {
            text-align: center;
            margin-top: 1rem;
        }
    `;

    @state() private name = '';
    @state() private email = '';
    @state() private role = '';
    @state() private targetEmail = '';
    @state() private error = '';
    @state() private rentals: any[] = [];
    @state() private page = 0;
    @state() private size = 5;
    @state() private returned = false;
    @state() private loadingRentals = true;

    async connectedCallback() {
        super.connectedCallback();
        const token = localStorage.getItem('accessToken');
        if (!token) {
            window.location.href = '/login';
            return;
        }
        const params = new URLSearchParams(location.search);
        this.targetEmail = params.get('email') ?? '';
        if (!this.targetEmail) {
            this.error = 'Missing email parameter in URL.';
            return;
        }

        try {
            const authRes = await fetch('/api/v1/users/self', {
                headers: { Authorization: `Bearer ${token}` }
            });
            const currentUser = await authRes.json();
            if (currentUser.role !== 'ADMIN') {
                this.error = 'Access denied. Only admins can view user details.';
                return;
            }

            await this.loadTargetUserProfile();
            await this.fetchRentals();
        } catch (err: any) {
            this.error = `Initialization error: ${err.message}`;
        }
    }

    private async loadTargetUserProfile() {
        const token = localStorage.getItem('accessToken');
        if (!token || !this.targetEmail) return;

        try {
            const res = await fetch(`/api/v1/users?email=${encodeURIComponent(this.targetEmail)}`, {
                headers: { Authorization: `Bearer ${token}` }
            });

            if (!res.ok) throw new Error(`Status ${res.status}`);
            const user = await res.json();
            this.name = user.name;
            this.email = user.email;
            this.role = user.role;
        } catch (err: any) {
            this.error = `Failed to load user: ${err.message}`;
        }
    }

    private async fetchRentals() {
        const token = localStorage.getItem('accessToken');
        if (!token || !this.targetEmail) return;
        this.loadingRentals = true;

        try {
            const res = await fetch(`/api/v1/rental/user/${encodeURIComponent(this.targetEmail)}/${this.page}/${this.size}/rentals?returned=${this.returned}`, {
                headers: { Authorization: `Bearer ${token}` }
            });

            if (!res.ok) throw new Error(`Status ${res.status}`);
            this.rentals = await res.json();
        } catch (err) {
            console.error('Rental fetch failed:', err);
            this.rentals = [];
        } finally {
            this.loadingRentals = false;
        }
    }

    private async changeReturned(flag: boolean) {
        this.page = 0;
        this.returned = flag;
        await this.fetchRentals();
    }

    private async nextPage() {
        this.page++;
        await this.fetchRentals();
    }

    private async prevPage() {
        if (this.page > 0) {
            this.page--;
            await this.fetchRentals();
        }
    }

    render() {
        return html`
      <section>
        <h2>User Profile</h2>
        ${this.error
            ? html`<div class="error">${this.error}</div>`
            : html`
              <p><strong>Name:</strong> ${this.name}</p>
              <p><strong>Email:</strong> ${this.email}</p>
              <p><strong>Role:</strong> ${this.role}</p>
            `}
      </section>

      ${!this.error && html`
        <section>
          <h3>User Rentals</h3>

          <div class="rental-controls">
            <button @click=${() => this.changeReturned(false)} ?disabled=${!this.returned}>Active</button>
            <button @click=${() => this.changeReturned(true)} ?disabled=${this.returned}>Returned</button>
          </div>

          ${this.loadingRentals
            ? html`<div>Loading rentals...</div>`
            : this.rentals.length === 0
                ? html`<p>No rentals found.</p>`
                : html`
                  ${this.rentals.map(r => html`
                    <div class="rentals-card">
                      <p><strong>${r.movieTitle}</strong></p>
                      <p>Rented: ${formatter.format(new Date(r.rentedAt))}</p>
                      <p>Due: ${formatter.format(new Date(r.dueDate))}</p>
                    </div>
                  `)}
                  <div class="pagination">
                    <button @click=${this.prevPage} ?disabled=${this.page === 0}>Previous</button>
                    <button @click=${this.nextPage}>Next</button>
                  </div>
                `}
        </section>
      `}
    `;
    }
}
