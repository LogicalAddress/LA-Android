package ng.com.nhub.paygis;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import ng.com.nhub.paygis.etc.BuildVars;
import ng.com.nhub.paygis.lib.AndroidUtilities;
import ng.com.nhub.paygis.lib.LocaleController;

public class AuthActivity extends AppCompatActivity implements
        RegisterFragment.OnFragmentInteractionListener,
        LoginFragment.OnFragmentInteractionListener{

    LoginFragment loginFragment;
    RegisterFragment registerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(BuildVars.DEBUG_VERSION){
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setTheme(R.style.AppDebugTheme);
        }
        super.onCreate(savedInstanceState);

        if (AndroidUtilities.isTablet()) {
            // Use fragmented display for tablets - Lining the display side by side

            setContentView(R.layout.activity_auth);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            loginFragment = new LoginFragment();
            registerFragment = new RegisterFragment();

            registerFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.auth_fragment, registerFragment).commit();
            getSupportActionBar().setTitle(LocaleController.getString("register", R.string.register));

        }else{

//            Intent registerIntent = new Intent(AuthActivity.this, RegisterActivity.class);
            Intent registerIntent = new Intent(AuthActivity.this, LoginActivity.class);
            startActivity(registerIntent);
            finish();
        }

    }

    private Fragment getCurrentFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        String fragmentTag = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentByTag(fragmentTag);
        return currentFragment;
    }

    @Override
    public void onFragmentInteraction(String fragment) {

        if (fragment == "loginFragment"){

            // Bundle args = new Bundle();
            // args.putInt(LoginFragment.ARG_POSITION, position);
            // newFragment.setArguments(args);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the auth_fragment view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.auth_fragment, loginFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(LocaleController.getString("login", R.string.login));

        }else if(fragment == "registerFragment"){
            onBackPressed();
        }else if (fragment == "principalFragment"){
            //TODO: to be removed. for testing purpose only.
            Intent intent2 = new Intent(AuthActivity.this, PrincipalActivity.class);
            startActivity(intent2);
            finish();
        }

    }

    @Override
    public void onBackPressed() {

        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            //additional code
        } else {

            Fragment currentFragment = getCurrentFragment();

            if(currentFragment instanceof LoginFragment) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setTitle(LocaleController.getString("register", R.string.register));
            }
            getFragmentManager().popBackStack();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
            }
        }
        return (super.onOptionsItemSelected(menuItem));
    }


}
