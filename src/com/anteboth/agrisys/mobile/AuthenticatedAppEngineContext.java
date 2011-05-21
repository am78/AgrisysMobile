package com.anteboth.agrisys.mobile;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AccountsException;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;

/**
 * This class handles the google app engine authentication process.
 *  
 * @author Alexander Eggers
 */
public final class AuthenticatedAppEngineContext implements HttpContext {
	
	private HttpContext delegate_;
	private CookieStore cookieStore_;

	/**
	 * Get a new AuthenticatedAppEngineContext instance for the obtained context and uri.
	 *  
	 * @param context the application context, must not be null
	 * @param uri the uri, shouldn't be null
	 * @return the AuthenticatedAppEngineContext instance
	 * 
	 * @throws AccountsException
	 * @throws AuthenticationException
	 */
	public static HttpContext newInstance(Context context, String uri)
	throws AccountsException, AuthenticationException {
		if (context == null) {
			throw new IllegalArgumentException("context is null");
		}
		return new AuthenticatedAppEngineContext(context, uri);
	}

	private AuthenticatedAppEngineContext(Context context, String uri)
	throws AccountsException, AuthenticationException {
		delegate_ = new BasicHttpContext();
		String authToken = getAuthenticationToken(context);
		AndroidHttpClient httpClient = AndroidHttpClient.newInstance("GetAuthCookieClient");//, context);
		try {
			httpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
			cookieStore_ = new BasicCookieStore();
			setAttribute(ClientContext.COOKIE_STORE, cookieStore_);
			HttpGet http_get = new HttpGet(
					uri + "/_ah/login?continue=http://localhost/&auth=" + authToken);
			HttpResponse response = httpClient.execute(http_get, this);
			checkResponse(cookieStore_, response);
		} catch (IOException e) {
			throw new AuthenticationException("error getting the authentication cookie", e);
		} finally {
			httpClient.close();
		}

	}

	private void checkResponse(CookieStore cookieStore, HttpResponse response)
	throws AuthenticationException {
		if (response.getStatusLine().getStatusCode() != 302) {
			throw new AuthenticationException("authentication response was not a redirect");
		}
		if (!isAuthenticationCookiePresent(cookieStore)) {
			throw new AuthenticationException("authentication cookie not found in cookie store");
		}
	}

	/**
	 * Uses the first com.google account and tries to get the authentication token for this 
	 * account.
	 * 
	 * @param context the application context
	 * @return the auth token
	 * @throws AccountsException
	 */
	private String getAuthenticationToken(Context context)
	throws AccountsException {
		AccountManager accountManager = AccountManager.get(context);
		Account[] accounts = accountManager.getAccountsByType("com.google");

		if (accounts == null || accounts.length == 0) {
			throw new AccountsException("no account of type 'com.google' found on this device");
		}

		try {
			Account account = accounts[0];
			AccountManagerFuture<Bundle> accountManagerFuture = 
				accountManager.getAuthToken(account, "ah", true, null, null);
			Bundle authTokenBundle = null;
			authTokenBundle = accountManagerFuture.getResult();
			String authToken = authTokenBundle.get(AccountManager.KEY_AUTHTOKEN).toString();
			return authToken;
		} catch (OperationCanceledException e) {
			throw new AccountsException("could not get authentication token from account 'com.google'", e);
		} catch (AuthenticatorException e) {
			throw new AccountsException("could not get authentication token from account 'com.google'", e);
		} catch (IOException e) {
			throw new AccountsException("could not get authentication token from account 'com.google'", e);
		}
	}

	private boolean isAuthenticationCookiePresent(CookieStore cookieStore) {
		for (Cookie cookie : cookieStore.getCookies()) {
			if (cookie.getName().equals("ACSID") || cookie.getName().equals("SACSID")) {
				return true;
			}
		}
		return false;
	}

	public Object getAttribute(String id) {
		return delegate_.getAttribute(id);
	}

	public Object removeAttribute(String id) {
		return delegate_.removeAttribute(id);
	}

	public void setAttribute(String id, Object obj) {
		delegate_.setAttribute(id, obj);
	}
}