import { fixture, html } from '@open-wc/testing';
import { expect, vi, describe, it, beforeEach } from 'vitest';
import '../login-form.ts';
import { LoginForm } from '../login-form';

describe('<login-form>', () => {
    let el: LoginForm;
    let shadow: ShadowRoot;

    beforeEach(async () => {
        el = await fixture<LoginForm>(html`<login-form></login-form>`);
        await el.updateComplete;
        shadow = el.shadowRoot!;
        vi.stubGlobal('location', { href: '' });
    });

    it('renders inputs and submit button', () => {
        expect(shadow.querySelectorAll('input').length).toBe(2);
        expect(shadow.querySelector('button')?.textContent).toMatch(/login/i);
    });

    it('saving token in local storage', async () => {
        localStorage.clear();

        const mockResponse = {
            ok: true,
            json: () => Promise.resolve({
                accessToken: 'token123',
                refreshToken: 'refresh456',
            }),
        };
        window.fetch = vi.fn().mockResolvedValue(mockResponse);

        vi.stubGlobal('location', { href: '' });

        const el = await fixture<LoginForm>(html`<login-form></login-form>`);
        const shadow = el.shadowRoot!;

        const emailInput = shadow.querySelector('input[placeholder="Email"]') as HTMLInputElement;
        const passwordInput = shadow.querySelector('input[placeholder="Password"]') as HTMLInputElement;
        const button = shadow.querySelector('button')!;

        emailInput.value = 'arek@mail.com';
        passwordInput.value = 'sekret123';
        emailInput.dispatchEvent(new Event('input', { bubbles: true }));
        passwordInput.dispatchEvent(new Event('input', { bubbles: true }));

        button.click();

        await el.updateComplete;

        await new Promise(resolve => setTimeout(resolve, 0));

        expect(window.fetch).toHaveBeenCalledTimes(1);
        expect(window.fetch).toHaveBeenCalledWith('/api/v1/auth/authenticate', expect.objectContaining({
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: 'arek@mail.com',
                password: 'sekret123',
            }),
        }));
        expect(localStorage.getItem('accessToken')).toBe('token123');
        expect(localStorage.getItem('refreshToken')).toBe('refresh456');
    });
});
