/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.conf;

import android.content.Context;
import android.util.Log;

import com.ca.mas.core.MobileSsoListener;
import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.error.MAGRuntimeException;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.oauth.GrantProvider;
import com.ca.mas.core.store.StorageProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;
import static com.ca.mas.core.conf.Config.HOSTNAME;
import static com.ca.mas.core.conf.Config.PORT;

public class ConfigurationManager {

    private final String CONNECTED_GATEWAY_CONFIG = "connected_gateway.json";
    private ConfigurationProvider connectedGatewayConfigurationProvider = null;
    private Context appContext;
    private List<Config> appConfigs;
    private String configurationFileName = null;
    private boolean enablePKCE = true;

    private MAGRequest.MAGConnectionListener connectionListener;
    private MobileSsoListener mobileSsoListener;

    private GrantProvider defaultGrantProvider = GrantProvider.PASSWORD;

    private static ConfigurationManager instance = new ConfigurationManager();

    private List<ConfigurationListener> configurationListeners = new ArrayList<>();

    private int certificateAdvancedRenewTimeframe = 30;

    private ConfigurationManager() {
        configurationListeners.add(new ClientChangeListener());
    }

    public static ConfigurationManager getInstance() {
        return instance;
    }

    public Context getContext() {
        return appContext;
    }

    public void enablePKCE(boolean enablePKCE) {
        this.enablePKCE = enablePKCE;
    }

    public boolean isPKCEEnabled() {
        return enablePKCE;
    }

    public void reset() {
        connectedGatewayConfigurationProvider = null;
    }

    public void init(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public void setAppConfigs(List<Config> appConfigs) {
        this.appConfigs = appConfigs;
    }

    public Server getConnectedGateway() {
        load();
        if (connectedGatewayConfigurationProvider != null) {
            return connectedGatewayConfigurationProvider.getServer();
        }
        throw new IllegalStateException("Gateway configuration should be configured.");
    }


    public JSONObject getConnectedGatewayConfig() {
        load();
        return connectedGatewayConfigurationProvider.getRaw();
    }

    public ConfigurationProvider getConnectedGatewayConfigurationProvider() {
        load();
        return connectedGatewayConfigurationProvider;
    }

    private void store(JSONObject config) {
        OutputStreamWriter writer = null;
        try {
            Context appContext = this.appContext;
            writer = new OutputStreamWriter(
                    appContext.openFileOutput(CONNECTED_GATEWAY_CONFIG, Context.MODE_PRIVATE));
            writer.write(config.toString());
            writer.flush();
        } catch (IOException e) {
            throw new MAGRuntimeException(MAGErrorCode.FAILED_JSON_SERIALIZATION, e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    //Ignore
                }
            }
        }
    }

    private void load() {
        if (connectedGatewayConfigurationProvider != null) return;
        InputStream is = null;
        StringBuilder jsonConfig = new StringBuilder();
        try {
            Context appContext = this.appContext;
            is = appContext.openFileInput(CONNECTED_GATEWAY_CONFIG);
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null) {
                jsonConfig.append(str);
            }
            connectedGatewayConfigurationProvider = create(new JSONObject(jsonConfig.toString()));
        } catch (IOException | JSONException e) {
            //Unable to load the cached one.
            activateDefault();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    //Ignore
                }
            }
        }
    }


    public void activateDefault() {
        JSONObject jsonObject = getConfig(getConfigurationFileName());
        activate(jsonObject);
    }

    public void activate(JSONObject jsonObject) {
        try {
            this.connectedGatewayConfigurationProvider = create(jsonObject);
            Context appContext = this.appContext;
            if (DEBUG) Log.d(TAG,
                    String.format("Activate configuration: %s", jsonObject.toString(4)));
            for (ConfigurationListener c : configurationListeners) {
                c.onUpdated(appContext, connectedGatewayConfigurationProvider);
            }
        } catch (JSONException e) {
            throw new MAGRuntimeException(MAGErrorCode.FAILED_JSON_VALIDATION, e);
        }
        store(jsonObject);
    }

    private JSONObject getConfig(String filename) {
        InputStream is = null;
        try {
            Context appContext = this.appContext;
            is = appContext.getAssets().open(filename);
            return getConfig(is);
        } catch (IOException e) {
            throw new MAGRuntimeException(MAGErrorCode.FAILED_FILE_NOT_FOUND, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public JSONObject getConfig(InputStream is) {
        StringBuilder jsonConfig = new StringBuilder();

        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null) {
                jsonConfig.append(str);
            }
            return new JSONObject(jsonConfig.toString());
        } catch (IOException e) {
            throw new MAGRuntimeException(MAGErrorCode.FAILED_FILE_NOT_FOUND, e);
        } catch (JSONException e) {
            throw new MAGRuntimeException(MAGErrorCode.INVALID_JSON, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }

        }
    }

    public ConfigurationProvider create(JSONObject jsonObject) throws JSONException {

        String tokenHost = getValue(HOSTNAME, jsonObject);
        String tokenUriPrefix = getValue(Config.PREFIX, jsonObject);
        Integer port = getValue(PORT, jsonObject);
        String clientId = getValue(Config.CLIENT_KEY, jsonObject);
        String clientSecret = getValue(Config.CLIENT_SECRET, jsonObject);
        String organization = getValue(Config.ORGANIZATION, jsonObject);

        DefaultConfiguration conf = new DefaultConfiguration(jsonObject, tokenHost, port, tokenUriPrefix, clientId, clientSecret, organization);

        Config[] attrs = Config.values;
        for (Config attr : attrs) {
            if (attr == Config.SERVER_CERTS) {
                List<String> trustedCerts = getValue(Config.SERVER_CERTS, jsonObject);
                if (trustedCerts != null && trustedCerts.size() > 0) {
                    conf.addTrustedCertificateAnchors(trustedCerts.toArray(new String[trustedCerts.size()]));
                }
                continue;
            }
            if (attr == Config.TRUSTED_CERT_PINNED_PUBLIC_KEY_HASHES) {
                List<String> trustedPins = getValue(Config.TRUSTED_CERT_PINNED_PUBLIC_KEY_HASHES, jsonObject);
                if (trustedPins != null && trustedPins.size() > 0) {
                    conf.addTrustedCertificatePinnedPublicKeyHashes(trustedPins.toArray(new String[trustedPins.size()]));
                }
                continue;
            }

            if (attr == Config.TRUSTED_PUBLIC_PKI) {
                conf.setAlsoTrustPublicPki((Boolean) getValue(Config.TRUSTED_PUBLIC_PKI, jsonObject, Boolean.FALSE));
                continue;
            }
            conf.putProperty(attr.key, getValue(attr, jsonObject));
        }

        //Load Application specific configuration
        if (appConfigs != null) {
            for (Config appConfig : appConfigs) {
                conf.putProperty(appConfig.key, getValue(appConfig, jsonObject));
            }
        }

        return conf;
    }

    private <T> T getValue(Config attr, JSONObject jsonObject) throws JSONException {
        return getValue(attr, jsonObject, null);
    }

    /**
     * Perform configure attribute validation and populate the Configure Bundle
     * for the SDK from the provided JSON Configuration.
     *
     * @param attr       The Configuration attribute
     * @param jsonObject The JSON Configuration object.
     * @throws JSONException When the JSON Configuration is invalid.
     */
    private <T> T getValue(Config attr, JSONObject jsonObject, Object def) throws JSONException {
        Object value = getValue(attr.path, attr.mandatory, jsonObject);
        if (value == null) {
            return (T) def;
        }
        if (!attr.type.isAssignableFrom(value.getClass()) &&
                !(value instanceof JSONArray && attr.type.isAssignableFrom(List.class)) &&
                !(value instanceof JSONObject && attr.type.isAssignableFrom(String.class))) {
            throw new MAGRuntimeException(MAGErrorCode.FAILED_JSON_VALIDATION, "Invalid value for attribute " + attr.path);
        }

        if (value instanceof String) {
            if (((String) value).trim().length() != 0) {
                return (T) value;
            } else {
                if (attr.mandatory) {
                    throw new MAGRuntimeException(MAGErrorCode.FAILED_JSON_VALIDATION, "Invalid value for attribute " + attr.path);
                }
            }
        } else if (value instanceof JSONArray) {
            ArrayList<String> list = new ArrayList<String>();
            JSONArray jsonArray = (JSONArray) value;
            for (int i = 0; i < jsonArray.length(); i++) {
                String v = null;
                Object a = jsonArray.get(i);
                if (a instanceof JSONArray) {
                    v = ((JSONArray) a).join("\n");
                    v = v.replace("\"", "");
                } else {
                    v = a.toString();
                }
                list.add(v);
            }
            return (T) list;
        }
        return (T) value;
    }

    /**
     * Retrieve the value from the JSON Configuration by the JSON path. The path use . notation for nested JSON object.
     * For example, mag.system_endpoints.device_register_endpoint_path
     *
     * @param path      The JSON path.
     * @param mandatory True for mandatory and False for not mandatory.
     * @param parent    The parent JSON Object, it can be a JSON Object or JSON Array.
     * @return The Value of the attribute.
     * @throws JSONException When the JSON message is invalid.
     */
    private static Object getValue(String path, boolean mandatory, Object parent) throws JSONException {
        if (parent == null) return null;
        int d = path.indexOf(".");
        if (d > 0) {
            String prefix = path.substring(0, d);
            if (parent instanceof JSONArray) {
                int index = Integer.parseInt(prefix);
                JSONArray array = (JSONArray) parent;
                return getValue(path.substring(d + 1), mandatory, array.get(index));
            } else {
                JSONObject jsonObject = (JSONObject) parent;
                Object nested = null;
                if (mandatory) {
                    nested = jsonObject.get(prefix);
                } else {
                    nested = jsonObject.opt(prefix);
                }
                return getValue(path.substring(d + 1), mandatory, nested);
            }
        } else {
            JSONObject jsonObject = (JSONObject) parent;
            if (mandatory) {
                return jsonObject.get(path);
            } else {
                return jsonObject.opt(path);
            }
        }
    }


    public String getConfigurationFileName() {
        if (configurationFileName == null) {
            return "msso_config.json";
        } else {
            return configurationFileName;
        }
    }

    public void setConfigurationFileName(String configurationFileName) {
        this.configurationFileName = configurationFileName;
    }

    public MAGRequest.MAGConnectionListener getConnectionListener() {
        return connectionListener;
    }

    public void setConnectionListener(MAGRequest.MAGConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    public MobileSsoListener getMobileSsoListener() {
        return mobileSsoListener;
    }

    public void setMobileSsoListener(MobileSsoListener mobileSsoListener) {
        this.mobileSsoListener = mobileSsoListener;
    }

    public GrantProvider getDefaultGrantProvider() {
        return defaultGrantProvider;
    }

    public void setDefaultGrantProvider(GrantProvider defaultGrantProvider) {
        this.defaultGrantProvider = defaultGrantProvider;
    }

    /**
     * For any client id change, we consider there is an update of the client,
     * clean up the stored client id, client secret, and tokens.
     */
    private static class ClientChangeListener implements ConfigurationListener {

        private static final String TAG = ClientChangeListener.class.getCanonicalName();

        @Override
        public void onUpdated(Context context, ConfigurationProvider provider) {
            StorageProvider.getInstance().reset();

            if (!StorageProvider.getInstance().hasValidStore()) {
                Log.w(TAG, "Failed to access the secure device storage, " +
                        "please verify the storage configuration, and make sure the device " +
                        "has Secure lock screen setup.");
                return;
            }

            String masterClientId = StorageProvider.getInstance()
                    .getClientCredentialContainer()
                    .getMasterClientId();

            //The masterClientId may be null due to SDK upgrade.
            if (masterClientId == null || !masterClientId.equals(provider.getClientId())) {
                StorageProvider.getInstance().getClientCredentialContainer().clear();
                StorageProvider.getInstance().getOAuthTokenContainer().clear();
            }
        }
    }

    /**
     * Listener to listen for configuration update
     */
    private interface ConfigurationListener {
        void onUpdated(Context context, ConfigurationProvider provider);
    }

    public int getCertificateAdvancedRenewTimeframe() {
        return certificateAdvancedRenewTimeframe;
    }

    public void setCertificateAdvancedRenewTimeframe(int certificateAdvancedRenewTimeframe) {
        this.certificateAdvancedRenewTimeframe = certificateAdvancedRenewTimeframe;
    }
}
