package be.blinkt.openvpn.core;

/**
 * Created by jan on 9/22/16.
 */

public class AppConfiguration {
    private String userConfiguration, commonConfiguration, allowedApps;

    public String getUserConfiguration() {
        return userConfiguration;
    }

    public String getCommonConfiguration() {
        return commonConfiguration;
    }

    public String getAllowedApps() {
        return allowedApps;
    }

    public void setUserConfiguration(String userConfiguration) {
        this.userConfiguration = userConfiguration;
    }

    public void setCommonConfiguration(String commonConfiguration) {
        this.commonConfiguration = commonConfiguration;
    }

    public void setAllowedApps(String allowedApps) {
        this.allowedApps = allowedApps;
    }
}
