import { html, css, LitElement } from 'lit';
import { customElement, state, query } from 'lit/decorators.js';

const formatter = new Intl.DateTimeFormat('pl-PL', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
});

@customElement('user-self')
export class UserSelf extends LitElement {
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
    @state() private error = '';
    @state() private editName = '';
    @state() private newPassword = '';
    @state() private successMsg = '';
    @state() private errorMsg = '';
    @query('dialog') private dialog!: HTMLDialogElement;

    @state() private rentals: any[] = [];
    @state() private page = 0;
    @state() private size = 5;
    @state() private returned = false;
    @state() private loadingRentals = true;
    @state() private rentalMsg = '';
    @state() private rentalError = false;

    async connectedCallback() {
        super.connectedCallback();
        const token = localStorage.getItem('accessToken');
        if (!token) return;

        try {
            const res = await fetch('/api/v1/users/self', {
                headers: {Authorization: `Bearer ${token}`}
            });

            if (!res.ok) throw new Error(`Status ${res.status}`);
            const user = await res.json();
            this.name = user.name;
            this.email = user.email;
            this.role = user.role;
            this.editName = user.name;
        } catch (err: any) {
            this.error = `Profile error: ${err.message}`;
        }

        await this.fetchRentals();
    }

    private async fetchRentals() {
        const token = localStorage.getItem('accessToken');
        if (!token) return;
        this.loadingRentals = true;

        try {
            const res = await fetch(`/api/v1/rental/self/${this.page}/${this.size}/rentals?returned=${this.returned}`, {
                headers: {Authorization: `Bearer ${token}`}
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

    private async returnMovie(id: number) {
        const token = localStorage.getItem('accessToken');
        if (!token) return;

        try {
            const res = await fetch(`/api/v1/rental/${id}/return`, {
                method: 'PUT',
                headers: {Authorization: `Bearer ${token}`}
            });

            if (!res.ok) throw new Error(`Return status ${res.status}`);
            this.rentalMsg = 'Movie returned.';
            this.rentalError = false;
            await this.fetchRentals();
        } catch (err: any) {
            this.rentalMsg = `Return error: ${err.message}`;
            this.rentalError = true;
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

    private openDialog() {
        this.dialog?.showModal();
    }

    private closeDialog() {
        this.dialog?.close();
        this.successMsg = '';
        this.errorMsg = '';
    }

    private async updateField(field: 'name' | 'password') {
        this.successMsg = '';
        this.errorMsg = '';
        const token = localStorage.getItem('accessToken');
        if (!token) {
            this.errorMsg = 'No token found.';
            return;
        }

        const params = new URLSearchParams();
        if (field === 'name' && this.editName.trim()) params.append('name', this.editName.trim());
        if (field === 'password' && this.newPassword.trim()) params.append('password', this.newPassword.trim());
        if (!params.toString()) {
            this.errorMsg = 'Enter value to update.';
            return;
        }

        try {
            const res = await fetch(`/api/v1/users/self?${params.toString()}`, {
                method: 'PUT',
                headers: {Authorization: `Bearer ${token}`}
            });

            if (!res.ok) throw new Error(`Update failed (${res.status})`);
            const result = await res.json();
            if (field === 'name') this.name = result.name;
            this.successMsg = field === 'name' ? 'Name updated.' : 'Password updated.';
            this.newPassword = '';
        } catch (err: any) {
            this.errorMsg = `Update error: ${err.message}`;
        }
    }

    render() {
        return html`
            <section>
                <h2>Your Profile</h2>
                ${this.error
                        ? html`
                            <div class="error">${this.error}</div>`
                        : html`
                            <p><strong>Name:</strong> ${this.name}</p>
                            <p><strong>Email:</strong> ${this.email}</p>
                            <p><strong>Role:</strong> ${this.role}</p>
                            <button @click=${this.openDialog}>Edit Profile</button>
                        `}
            </section>

            <section>
                <h3>Your Rentals</h3>

                <div class="rental-controls">
                    <button @click=${() => this.changeReturned(false)} ?disabled=${!this.returned}>Active</button>
                    <button @click=${() => this.changeReturned(true)} ?disabled=${this.returned}>Returned</button>
                </div>

                ${this.loadingRentals
                        ? html`
                            <div>Loading rentals...</div>`
                        : this.rentals.length === 0
                                ? html`<p>No rentals found.</p>`
                                : html`
                                    ${this.rentals.map(r => html`
                                        <div class="rentals-card">
                                            <p><strong>${r.movieTitle}</strong></p>
                                            <p>Rented: ${formatter.format(new Date(r.rentedAt))}</p>
                                            <p>Due: ${formatter.format(new Date(r.dueDate))}</p>
                                            ${!this.returned
                                                    ? html`
                                                        <button @click=${() => this.returnMovie(r.movieId)}>Return</button>`
                                                    : null}
                                        </div>
                                    `)}
                                    <div class="pagination">
                                        <button @click=${this.prevPage} ?disabled=${this.page === 0}>Previous</button>
                                        <button @click=${this.nextPage}>Next</button>
                                    </div>
                                `
                }
                ${this.rentalMsg && html`
                    <div class=${this.rentalError ? 'error' : 'success'}>
                        ${this.rentalMsg}
                    </div>
                `}
            </section>

            <dialog @click=${(e: Event) => {
                if ((e.target as HTMLElement).tagName === 'DIALOG') this.closeDialog();
            }}>
                <h3>Update Profile</h3>
                <input
                        type="text"
                        placeholder="New name"
                        .value=${this.editName}
                        @input=${(e: Event) => this.editName = (e.target as HTMLInputElement).value}
                />
                <button @click=${() => this.updateField('name')}>Update Name</button>

                <input
                        type="password"
                        placeholder="New password"
                        .value=${this.newPassword}
                        @input=${(e: Event) => this.newPassword = (e.target as HTMLInputElement).value}
                />
                <button @click=${() => this.updateField('password')}>Update Password</button>

                ${this.errorMsg && html`
                    <div class="error">${this.errorMsg}</div>`}
                ${this.successMsg && html`
                    <div class="success">${this.successMsg}</div>`}

                <button @click=${this.closeDialog}>Close</button>
            </dialog>
        `;
    };
}