import {css, html, LitElement} from 'lit';
import {customElement, state} from 'lit/decorators.js';
import '@lion/form/define';
import '@lion/input/define';
import '@lion/select/define';
import '@lion/button/define';
import '@lion/dialog/define';


interface MovieDto {
    id: number;
    title: string;
    genre: string;
    available: boolean;
}

@customElement('movie-list')
export class MovieList extends LitElement {
    static styles = css`
        section {
            max-width: 800px;
            margin: 2rem auto;
            padding: 1.5rem;
            background: #fff;
            border-radius: 8px;
            box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
        }

        h2 {
            margin-bottom: 1rem;
            color: #2e7d32;
        }

        lion-form {
            margin-bottom: 1.5rem;
            padding: 1rem;
            background: #f9f9f9;
            border-radius: 6px;
        }

        lion-input,
        lion-select {
            margin-bottom: 1rem;
        }

        lion-button {
            --button-background-color: #2e7d32;
            --button-color: white;
            cursor: pointer;
        }

        ul {
            list-style: none;
            padding: 0;
        }

        li.movie-card {
            padding: 0.8rem 1rem;
            margin-bottom: 0.6rem;
            background: #f5f5f5;
            border-radius: 6px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .details {
            flex: 1;
        }

        .available {
            color: #2e7d32;
            font-weight: bold;
        }

        .unavailable {
            color: #c62828;
            font-weight: bold;
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

        .loading {
            text-align: center;
            font-style: italic;
        }
    `;

    @state() private isPopupOpen = false;
    @state() private movies: MovieDto[] = [];
    @state() private loading = true;
    @state() private message = '';
    @state() private isError = false;
    @state() private isAdmin = false;
    @state() private editingMovieId: number | null = null;
    @state() private editValues: { title: string; genre: string; status: boolean } = {
        title: '',
        genre: '',
        status: true,
    };

    private openEditDialog(movie: MovieDto) {
        this.editingMovieId = movie.id;
        this.editValues = {
            title: movie.title,
            genre: movie.genre,
            status: movie.available
        };
        this.isPopupOpen = true;
    }

    private decodeJwt(token: string): any {
        try {
            const payload = token.split('.')[1];
            const decoded = atob(payload);
            return JSON.parse(decoded);
        } catch (e) {
            return null;
        }
    }

    async connectedCallback() {
        super.connectedCallback();

        const token = localStorage.getItem('accessToken');
        if (!token) {
            window.location.href = '/login';
            return;
        }

        const decoded = this.decodeJwt(token);
        this.isAdmin = decoded?.role === 'ADMIN' || decoded?.roles?.includes?.('ADMIN');

        try {
            const res = await fetch('/api/v1/movies/list', {
                headers: { Authorization: `Bearer ${token}` }
            });

            if (res.status === 403) {
                window.location.href = '/login';
                return;
            }

            if (!res.ok) {
                this.message = `Could not fetch movies. Status: ${res.status}`;
                this.isError = true;
                return;
            }

            const data = await res.json();
            this.movies = Array.isArray(data) ? data : [];
        } catch (err: any) {
            this.message = `Fetch error: ${err.message}`;
            this.isError = true;
        } finally {
            this.loading = false;
        }
    }

    private async createMovie() {
        const token = localStorage.getItem('accessToken');
        const form = this.renderRoot.querySelector('lion-form');
        if (!form || !token) return;

        const payload = form.serializedValue;
        payload.available=true

        try {
            const res = await fetch('/api/v1/movies', {
                method: 'POST',
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            if (!res.ok) {
                this.message = `Create failed. Status: ${res.status}`;
                this.isError = true;
                return;
            }

            const created: MovieDto = await res.json();
            this.movies = [...this.movies, created];
            this.message = `Movie "${created.title}" added`;
            this.isError = false;
        } catch (err: any) {
            this.message = `Error: ${err.message}`;
            this.isError = true;
        }
    }

    private async deleteMovie(id: number) {
        const token = localStorage.getItem('accessToken');
        if (!token) return;

        try {
            const res = await fetch(`/api/v1/movies/${id}`, {
                method: 'DELETE',
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!res.ok) {
                this.message = `Delete failed. Status: ${res.status}`;
                this.isError = true;
                return;
            }

            this.movies = this.movies.filter(m => m.id !== id);
            this.message = `Movie deleted successfully`;
            this.isError = false;
        } catch (err: any) {
            this.message = `Error deleting movie: ${err.message}`;
            this.isError = true;
        }
    }
    private async submitUpdate() {
        const token = localStorage.getItem('accessToken');
        if (!token || this.editingMovieId === null) return;

        const params = new URLSearchParams({
            id: String(this.editingMovieId),
            title: String(this.editValues.title),
            genre: String(this.editValues.genre),
            status: String(this.editValues.status)
        });

        console.log(this.editValues)
        try {
            const res = await fetch(`/api/v1/movies?${params.toString()}`, {
                method: 'PUT',
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!res.ok) {
                this.message = `Update failed. Status: ${res.status}`;
                this.isError = true;
                return;
            }

            const updated: MovieDto = await res.json();
            this.movies = this.movies.map(m =>
                m.id === updated.id ? updated : m
            );
            this.message = `Movie "${updated.title}" updated successfully`;
            this.isError = false;
        } catch (err: any) {
            this.message = `Error updating movie: ${err.message}`;
            this.isError = true;
        } finally {
            this.isPopupOpen = false;
            this.editingMovieId = null;
        }
    }

    private closeEditDialog() {
        this.isPopupOpen = false;
        this.editingMovieId = null;
    }

    render() {
        return html`
      <section>
        <h2>Movie List</h2>

        ${this.isAdmin
            ? html`
              <lion-form>
                <lion-input name="title" label="Title"></lion-input>
                <lion-select name="genre" label="Genre">
                  <select slot="input">
                    <option value="">Choose genre</option>
                    <option value="ACTION">Action</option>
                    <option value="DRAMA">Drama</option>
                    <option value="COMEDY">Comedy</option>
                    <option value="HORROR">Horror</option>
                    <option value="THRILLER">Thriller</option>
                    <option value="SCI_FI">Sci-fi</option>
                    <option value="FANTASY">Fantasy</option>
                  </select>
                </lion-select>
                <lion-button @click=${this.createMovie}>Add Movie</lion-button>
              </lion-form>
            `
            : null}

          ${this.loading
                  ? html`<div class="loading">Loading movies...</div>`
                  : this.isPopupOpen
                          ? html`
        <section class="popup-card">
          <h3>Edit Movie</h3>
          <lion-form>
              <lion-input
                      name="title"
                      label="Title"
                      .modelValue=${this.editValues.title}
                      @model-value-changed=${(e: CustomEvent) => {
                          const inputEl = e.target as HTMLInputElement;
                          this.editValues.title = inputEl.modelValue ?? ''
                      }}>
              </lion-input>
              <lion-select
                      name="genre"
                      label="Genre"
                      .modelValue=${this.editValues.genre}
                      @model-value-changed=${(e: CustomEvent) => {
                          const inputEl = e.target as HTMLInputElement;
                          this.editValues.genre = inputEl.modelValue ?? ''
                      }}>
                  <select slot="input">
                      <option value="ACTION">Action</option>
                      <option value="DRAMA">Drama</option>
                      <option value="COMEDY">Comedy</option>
                      <option value="HORROR">Horror</option>
                      <option value="THRILLER">Thriller</option>
                      <option value="SCI_FI">Sci-fi</option>
                      <option value="FANTASY">Fantasy</option>
                  </select>
              </lion-select>

            <lion-select
              name="status"
              label="Availability"
              .modelValue=${String(this.editValues.status)}
              @model-value-changed=${(e: CustomEvent) =>{
                  const inputEl = e.target as HTMLInputElement;
                  this.editValues.status = inputEl.modelValue ?? ''
              }}>
              <select slot="input">
                <option value="true">Available</option>
                <option value="false">Unavailable</option>
              </select>
            </lion-select>

            <lion-button @click=${this.submitUpdate}>Save</lion-button>
            <lion-button @click=${this.closeEditDialog} style="margin-left: 1rem;">
              Cancel
            </lion-button>
          </lion-form>
        </section>
      ` : html`
        <ul>
          ${this.movies.map((movie) => html`
            <li class="movie-card">
              <div class="details">
                <strong>${movie.title}</strong><br />
                Genre: ${movie.genre}<br />
                Status:
                <span class=${movie.available ? 'available' : 'unavailable'}>
                  ${movie.available ? 'Available' : 'Unavailable'}
                </span>
              </div>

              ${this.isAdmin ? html`
                <lion-button
                  style="background: #c62828; color: white;"
                  @click=${() => this.deleteMovie(movie.id)}>
                  Delete
                </lion-button>
                <lion-button
                  style="background: #0277bd; color: white;"
                  @click=${() => this.openEditDialog(movie)}>
                  Update
                </lion-button>
              ` : null}
                ${!this.isAdmin && movie.available ? html`
                      <lion-button
                        style="background: #2e7d32; color: white;"
                        @click=${() => window.location.href = `/rent?movieId=${movie.id}`}>
                        Rent
                      </lion-button>
                ` : null}
            </li>
          `)}
        </ul>
      `} ${this.message &&
        html`
            <div class="message ${this.isError ? 'error' : 'success'}">
              ${this.message}
            </div>
          `}
      </section>
    `;
    }
}