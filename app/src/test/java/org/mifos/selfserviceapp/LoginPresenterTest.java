package org.mifos.selfserviceapp;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifos.selfserviceapp.api.DataManager;
import org.mifos.selfserviceapp.api.local.PreferencesHelper;
import org.mifos.selfserviceapp.models.Page;
import org.mifos.selfserviceapp.models.User;
import org.mifos.selfserviceapp.models.client.Client;
import org.mifos.selfserviceapp.presenters.LoginPresenter;
import org.mifos.selfserviceapp.ui.views.LoginView;
import org.mifos.selfserviceapp.util.RxSchedulersOverrideRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import rx.Observable;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by dilpreet on 27/6/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class LoginPresenterTest {

    @Rule
    public final RxSchedulersOverrideRule mOverrideSchedulersRule = new RxSchedulersOverrideRule();

    @Mock
    Context context;

    @Mock
    DataManager dataManager;

    @Mock
    PreferencesHelper mockHelper;

    @Mock
    LoginView view;

    private LoginPresenter presenter;
    private User user;
    private Page<Client> clientPage, noClientPage;

    @Before
    public void setUp() throws Exception {

        when(dataManager.getPreferencesHelper()).thenReturn(mockHelper);

        presenter = new LoginPresenter(dataManager, context);
        presenter.attachView(view);

        user = FakeRemoteDataSource.getUser();
        clientPage = FakeRemoteDataSource.getClients();
        noClientPage = FakeRemoteDataSource.getNoClients();

        when(context.getString(R.string.error_fetching_client)).
                thenReturn("Failed to fetch Client");
        when(context.getString(R.string.error_client_not_found)).
                thenReturn("Client Not Found");
    }

    @Test
    public void testLogin() throws Exception {
        when(dataManager.login("selfservice", "password")).thenReturn(Observable.just(user));

        presenter.login("selfservice", "password");

        verify(view).showProgress();
        verify(view).hideProgress();
        verify(view).onLoginSuccess(user.getUserName());
    }

    @Test
    public void testLoadClients() throws Exception {
        long clientId = clientPage.getPageItems().get(0).getId();
        when(dataManager.getClients()).thenReturn(Observable.just(clientPage));

        presenter.loadClient();

        verify(view).showProgress();
        verify(view).hideProgress();
        verify(view).showClient(clientId);
        verify(view, never()).showMessage(context.getString(R.string.error_fetching_client));
    }

    @Test
    public void testLoadNoClients() throws Exception {
        long clientId = clientPage.getPageItems().get(0).getId();
        when(dataManager.getClients()).thenReturn(Observable.just(noClientPage));

        presenter.loadClient();

        verify(view).showProgress();
        verify(view).hideProgress();
        verify(view).showMessage(context.getString(R.string.error_client_not_found));
        verify(view, never()).showClient(clientId);
    }

    @Test
    public void testLoadClientFails() throws Exception {
        long clientId = clientPage.getPageItems().get(0).getId();
        when(dataManager.getClients()).thenReturn(Observable.<Page<Client>>error(new
                RuntimeException()));

        presenter.loadClient();

        verify(view).showProgress();
        verify(view).hideProgress();
        verify(view).showMessage(context.getString(R.string.error_fetching_client));
        verify(view, never()).showClient(clientId);
    }

    @After
    public void tearDown() throws Exception {
        presenter.detachView();
    }
}
