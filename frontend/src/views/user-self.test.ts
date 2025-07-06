import { fixture, html } from '@open-wc/testing';
import { expect, vi } from 'vitest';
import './user-self.ts';
import { UserSelf } from './user-self.ts';

describe('<user-self>', () => {
    beforeEach(() => {
        localStorage.clear();
        vi.restoreAllMocks();
    });

    it('renders profile data from fetch', async () => {
        localStorage.setItem('accessToken', 'token123');

        const mockUser = {
            name: 'Arkadiusz',
            email: 'arek@example.com',
            role: 'USER',
        };

        window.fetch = vi.fn().mockResolvedValue({
            ok: true,
            json: () => Promise.resolve(mockUser),
        });

        const el = await fixture<UserSelf>(html`<user-self></user-self>`);
        const shadow = el.shadowRoot!;

        await el.updateComplete;

        expect(shadow.textContent).toContain(mockUser.name);
        expect(shadow.textContent).toContain(mockUser.email);
        expect(shadow.textContent).toContain(mockUser.role);
    });

    it('shows error when token is missing', async () => {
        const el = await fixture<UserSelf>(html`<user-self></user-self>`);
        const shadow = el.shadowRoot!;
        expect(el.name).toBe('');
        expect(el.error).toBe('');
    });

    it('shows error when fetch returns 403', async () => {
        localStorage.setItem('accessToken', 'token123');

        window.fetch = vi.fn().mockResolvedValue({
            ok: false,
            status: 403,
        });

        const el = await fixture<UserSelf>(html`<user-self></user-self>`);
        const shadow = el.shadowRoot!;
        await el.updateComplete;
        expect(el.error).toMatch(/access denied/i);
        expect(shadow.querySelector('.error')?.textContent).toMatch(/access denied/i);
    });
});
