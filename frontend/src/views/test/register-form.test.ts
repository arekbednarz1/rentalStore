import { fixture, html } from '@open-wc/testing';
import { expect, vi } from 'vitest';
import '../register-form.ts';
import {RegisterForm} from "../register-form";

describe('<register-form>', () => {
    it('renders the form with correct fields', async () => {
        const el = await fixture(html`<register-form></register-form>`);
        const shadow = el.shadowRoot!;

        const inputs = shadow.querySelectorAll('input');
        expect(inputs.length).toBe(3);

        const button = shadow.querySelector('button');
        expect(button?.textContent).toMatch(/register/i);
    });
    it('saving token in local storage after successful registration', async () => {
        localStorage.clear();
        const mockResponse = {
            ok: true,
            json: () => Promise.resolve({
                accessToken: 'regToken123',
                refreshToken: 'regRefresh456',
            }),
        };
        window.fetch = vi.fn().mockResolvedValue(mockResponse);
        vi.stubGlobal('location', { href: '' });

        const el = await fixture<RegisterForm>(html`<register-form></register-form>`);
        const shadow = el.shadowRoot!;

        const usernameInput = shadow.querySelector('input[placeholder="User name"]') as HTMLInputElement;
        const emailInput = shadow.querySelector('input[placeholder="Email"]') as HTMLInputElement;
        const passwordInput = shadow.querySelector('input[placeholder="Password"]') as HTMLInputElement;
        const button = shadow.querySelector('button')!;

        usernameInput.value = 'arek';
        emailInput.value = 'arek@mail.com';
        passwordInput.value = 'sekret123';
        usernameInput.dispatchEvent(new Event('input', { bubbles: true }));
        emailInput.dispatchEvent(new Event('input', { bubbles: true }));
        passwordInput.dispatchEvent(new Event('input', { bubbles: true }));

        button.click();
        await el.updateComplete;
        await new Promise(resolve => setTimeout(resolve, 0));

        expect(window.fetch).toHaveBeenCalledTimes(1);
        expect(window.fetch).toHaveBeenCalledWith('/api/v1/auth/register', expect.objectContaining({
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: 'arek',
                email: 'arek@mail.com',
                password: 'sekret123',
            }),
        }));

        expect(localStorage.getItem('accessToken')).toBe('regToken123');
        expect(localStorage.getItem('refreshToken')).toBe('regRefresh456');
    });
});
