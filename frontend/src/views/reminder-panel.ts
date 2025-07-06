import { html, css, LitElement } from 'lit';
import { customElement, state } from 'lit/decorators.js';

const formatter = new Intl.DateTimeFormat('pl-PL', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
});

interface ReminderMessageDto {
    movieTitle: string;
    rentalId: number;
    rentedAt?: string;
    dueDate: string;
}

@customElement('reminder-panel')
export class ReminderPanel extends LitElement {
    static styles = css`
        section {
            max-width: 540px;
            margin: 2rem auto;
            padding: 1rem 1.5rem;
            background: #fefefe;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0,0,0,0.08);
        }

        h2 {
            color: #2e7d32;
        }
        .reminder-card {
            margin-bottom: 1rem;
            padding: 1rem;
            border: 1px solid #ccc;
            border-radius: 6px;
            background: #f9f9f9;
        }

        .urgent {
            border-color: #c62828;
            background: #ffecec;
        }

        .error {
            color: #c62828;
            font-weight: bold;
            text-align: center;
            margin-bottom: 1rem;
        }

        .message {
            color: #c62828;
            font-weight: bold;
            margin-bottom: 0.4rem;
        }
    `;

    @state() private reminders: ReminderMessageDto[] = [];
    @state() private error = '';
    @state() private role = '';
    @state() private loading = true;

    async connectedCallback() {
        super.connectedCallback();
        const token = localStorage.getItem('accessToken');
        if (!token) {
            window.location.href = '/login';
            return;
        }

        try {
            const userRes = await fetch('/api/v1/users/self', {
                headers: { Authorization: `Bearer ${token}` }
            });

            const user = await userRes.json();
            this.role = user.role;

            if (this.role === 'ADMIN') {
                this.error = 'Access denied. This view is for users only.';
                return;
            }

            const res = await fetch('/api/v1/rental/self/reminder', {
                headers: { Authorization: `Bearer ${token}` }
            });

            if (!res.ok) throw new Error(`Status ${res.status}`);
            const data = await res.json();
            this.reminders = Array.isArray(data) ? data : [];
            console.log('Reminders loaded:', this.reminders);
        } catch (err: any) {
            this.error = `Error: ${err.message}`;
        } finally {
            this.loading = false;
        }
    }

    private isUrgent(dueDate: string): boolean {
        const due = new Date(dueDate).getTime();
        const now = Date.now();
        const hoursLeft = (due - now) / (1000 * 60 * 60);
        return hoursLeft <= 24;
    }

    render() {
        return html`
            <section>
                <h2>Your Active Reminders</h2>

                ${this.error
                        ? html`<div class="error">${this.error}</div>`
                        : this.loading
                                ? html`<p>Loading reminders...</p>`
                                : this.reminders.length === 0
                                        ? html`<p>No active reminders found.</p>`
                                        : html`
                                            ${this.reminders.map(reminder => {
                                                const urgent = this.isUrgent(reminder.dueDate);
                                                const rentedDate = reminder.rentedAt
                                                        ? formatter.format(new Date(reminder.rentedAt))
                                                        : 'unknown';

                                                return html`
                                                    <div class="reminder-card ${urgent ? 'urgent' : ''}">
                                                        ${urgent ? html`<div class="message">⚠️ Please return immediately</div>` : null}
                                                        Title: <strong>${reminder.movieTitle}</strong><br />
                                                        Due date: ${formatter.format(new Date(reminder.dueDate))}
                                                    </div>
                                                `;
                                            })}
                                        `}
            </section>
        `;
    }
}
