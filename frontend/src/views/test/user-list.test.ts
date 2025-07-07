import { fixture, html, expect } from '@open-wc/testing';
import sinon from 'sinon';
import '../user-list';
import { UserList } from '../user-list';

describe('<user-list>', () => {
    let element: UserList;

    const mockUsers = [
        { name: 'John Doe', email: 'john@example.com', role: 'USER' },
        { name: 'Jane Admin', email: 'jane@example.com', role: 'ADMIN' },
    ];

    beforeEach(async () => {
        sinon.stub(window, 'fetch');
        localStorage.setItem('accessToken', 'mock-token');

        (window.fetch as sinon.SinonStub).resolves(new Response(JSON.stringify(mockUsers), {
            status: 200,
            headers: { 'Content-Type': 'application/json' }
        }));

        element = await fixture(html`<user-list></user-list>`);
        await element.updateComplete;
    });

    afterEach(() => {
        (window.fetch as sinon.SinonStub).restore();
        localStorage.clear();
    });

    it('should render user list', () => {
        const userCards = element.shadowRoot?.querySelectorAll('.user-card');
        expect(userCards?.length).to.equal(2);
    });

    it('should show error on delete 400', async () => {
        (window.fetch as sinon.SinonStub).onSecondCall().resolves(new Response(null, { status: 400 }));

        await element.deleteUser('jane@example.com');
        await element.updateComplete;

        expect(element.isError).to.be.true;
        expect(element.message).to.include('Cannot delete yourself');
    });
});
