package org.ei.opensrp.service;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.ei.opensrp.AllConstants;
import org.ei.opensrp.DristhiConfiguration;
import org.ei.opensrp.domain.LoginResponse;
import org.ei.opensrp.domain.Response;
import org.ei.opensrp.domain.TeamData;
import org.ei.opensrp.repository.AllSettings;
import org.ei.opensrp.repository.AllSharedPreferences;
import org.ei.opensrp.repository.Repository;
import org.ei.opensrp.sync.SaveANMLocationTask;
import org.ei.opensrp.sync.SaveTeamInfoTask;
import org.ei.opensrp.sync.SaveUserInfoTask;
import org.ei.opensrp.util.Session;
import org.ei.opensrp.util.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.api.domain.Location;
import org.opensrp.api.util.EntityUtils;
import org.opensrp.api.util.LocationTree;
import org.opensrp.api.util.TreeNode;

import java.util.Map;

import static java.text.MessageFormat.format;
import static org.ei.opensrp.AllConstants.*;
import static org.ei.opensrp.event.Event.ON_LOGOUT;
import static org.ei.opensrp.util.Log.logInfo;

public class UserService {
    private final Repository repository;
    private final AllSettings allSettings;
    private final AllSharedPreferences allSharedPreferences;
    private HTTPAgent httpAgent;
    private Session session;
    private DristhiConfiguration configuration;
    private SaveANMLocationTask saveANMLocationTask;
    private String village ="";
    private SaveUserInfoTask saveUserInfoTask;
    private SaveTeamInfoTask saveTeamInfoTask;

    public UserService(Repository repository, AllSettings allSettings, AllSharedPreferences allSharedPreferences, HTTPAgent httpAgent, Session session,
                       DristhiConfiguration configuration, SaveANMLocationTask saveANMLocationTask,
                       SaveUserInfoTask saveUserInfoTask,SaveTeamInfoTask saveTeamInfoTask) {
        this.repository = repository;
        this.allSettings = allSettings;
        this.allSharedPreferences = allSharedPreferences;
        this.httpAgent = httpAgent;
        this.session = session;
        this.configuration = configuration;
        this.saveANMLocationTask = saveANMLocationTask;
        this.saveUserInfoTask = saveUserInfoTask;
        this.saveTeamInfoTask = saveTeamInfoTask;
    }

    public boolean isValidLocalLogin(String userName, String password) {
        return allSharedPreferences.fetchRegisteredANM().equals(userName) && repository.canUseThisPassword(password);
    }

    public LoginResponse isValidRemoteLogin(String userName, String password) {
        String requestURL;

        requestURL = configuration.dristhiBaseURL() + OPENSRP_AUTH_USER_URL_PATH;

        return httpAgent.urlCanBeAccessWithGivenCredentials(requestURL, userName, password);
    }

    public Response<String> getLocationInformation() {
        String requestURL = configuration.dristhiBaseURL() + OPENSRP_LOCATION_URL_PATH;
        return httpAgent.fetch(requestURL);
    }

    private void loginWith(String userName, String password) {
        setupContextForLogin(userName, password);
        allSettings.registerANM(userName, password);
    }

    public void localLogin(String userName, String password) {
        loginWith(userName, password);
    }

    public void remoteLogin(String userName, String password, String userInfo) {
        loginWith(userName, password);
        saveAnmLocation(getUserLocation(userInfo));
        saveUserInfo(getUserData(userInfo));
        saveUserTeam(getUserTeam(userInfo));

        TeamData teamData = new Gson().fromJson(allSettings.fetchTeamInformation(),
                new TypeToken<TeamData>() {
                }.getType());
        allSharedPreferences.setPreference(AllConstants.SyncFilters.FILTER_LOCATION_ID, teamData.locationName());
        logInfo(format("team location:  {0}", teamData.locationName()));
    }

    public String getUserData(String userInfo) {
        try {
            JSONObject userInfoJson = new JSONObject(userInfo);
            return userInfoJson.getString("user");
        } catch (JSONException e) {
            Log.v("Error : ", e.getMessage());
            return null;
        }
    }

    public String getUserLocation(String userInfo) {
        try {
            JSONObject userLocationJSON = new JSONObject(userInfo);
            return userLocationJSON.getString("locations");
        } catch (JSONException e) {
            Log.v("Error : ", e.getMessage());
            return null;
        }
    }

    public String getUserTeam(String userInfo) {
        try {
            JSONObject userInfoJson = new JSONObject(userInfo);
            return userInfoJson.getString("team");
        } catch (JSONException e) {
            Log.v("Error : ", e.getMessage());
            return null;
        }
    }

    public void saveAnmLocation(String anmLocation) {
        saveANMLocationTask.save(anmLocation);
    }

    public void saveUserInfo(String userInfo) { saveUserInfoTask.save(userInfo); }

    public void saveUserTeam(String teamData) {
        saveTeamInfoTask.save(teamData);
    }

    public boolean hasARegisteredUser() {
        return !allSharedPreferences.fetchRegisteredANM().equals("");
    }

    public void logout() {
        logoutSession();
        allSettings.registerANM("", "");
        allSettings.savePreviousFetchIndex("0");
        repository.deleteRepository();
    }

    public void logoutSession() {
        session().expire();
        ON_LOGOUT.notifyListeners(true);
    }

    public boolean hasSessionExpired() {
        return session().hasExpired();
    }

    protected void setupContextForLogin(String userName, String password) {
        session().start(session().lengthInMilliseconds());
        session().setPassword(password);
    }

    protected Session session() {
        return session;
    }

    public String switchLanguagePreference() {
        String preferredLocale = allSharedPreferences.fetchLanguagePreference();
        if (ENGLISH_LOCALE.equals(preferredLocale)) {
            allSharedPreferences.saveLanguagePreference(KANNADA_LOCALE);
            return KANNADA_LANGUAGE;
        } else {
            allSharedPreferences.saveLanguagePreference(ENGLISH_LOCALE);
            return ENGLISH_LANGUAGE;
        }
    }
}