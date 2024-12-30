/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Base64;
import android.util.LongSparseArray;

import org.telegram.tgnet.AiModelBean;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

//用户配置
public class UserConfig extends BaseController {

    public static int selectedAccount;
    public final static int MAX_ACCOUNT_DEFAULT_COUNT = 3;
    public final static int MAX_ACCOUNT_COUNT = 4;

    public final static String IMAGE_TRANSCODE = "base64";

    private final Object sync = new Object();
    private volatile boolean configLoaded;
    private TLRPC.User currentUser;
    public boolean registeredForPush;
    public int lastSendMessageId = !BuildVars.IS_CHAT_AIR ? -210000 : 1000;
    private int lastUserId = defaultUserId;
    public int lastBroadcastId = -1;
    public int contactsSavedCount;
    public long clientUserId;
    public int lastContactsSyncTime;
    public int lastHintsSyncTime;
    public boolean draftsLoaded;
    public boolean unreadDialogsLoaded = true;
    public TLRPC.TL_account_tmpPassword tmpPassword;
    public int ratingLoadTime;
    public int botRatingLoadTime;
    public boolean contactsReimported;
    public boolean hasValidDialogLoadIds;
    public int migrateOffsetId = -1;
    public int migrateOffsetDate = -1;
    public long migrateOffsetUserId = -1;
    public long migrateOffsetChatId = -1;
    public long migrateOffsetChannelId = -1;
    public long migrateOffsetAccess = -1;
    public boolean filtersLoaded;

    public int sharingMyLocationUntil;
    public int lastMyLocationShareTime;

    public boolean notificationsSettingsLoaded;
    public boolean notificationsSignUpSettingsLoaded;
    public boolean syncContacts = BuildVars.IS_CHAT_AIR ? false : true;
    public boolean suggestContacts = BuildVars.IS_CHAT_AIR ? false : true;
    public boolean hasSecureData;
    public int loginTime;
    public TLRPC.TL_help_termsOfService unacceptedTermsOfService;
    public long autoDownloadConfigLoadTime;

    public List<String> awaitBillingProductIds = new ArrayList<>();
    public TLRPC.InputStorePaymentPurpose billingPaymentPurpose;

    public String premiumGiftsStickerPack;
    public String genericAnimationsStickerPack;
    public String defaultTopicIcons;
    public long lastUpdatedPremiumGiftsStickerPack;
    public long lastUpdatedGenericAnimations;
    public long lastUpdatedDefaultTopicIcons;

    public volatile byte[] savedPasswordHash;
    public volatile byte[] savedSaltedPassword;
    public volatile long savedPasswordTime;
    LongSparseArray<SaveToGallerySettingsHelper.DialogException> userSaveGalleryExceptions;
    LongSparseArray<SaveToGallerySettingsHelper.DialogException> chanelSaveGalleryExceptions;
    LongSparseArray<SaveToGallerySettingsHelper.DialogException> groupsSaveGalleryExceptions;

    public LinkedHashMap<Integer, AiModelBean> aiModelList = new LinkedHashMap<>();
    public final static String defaultPrompt = "You are a helpful AI assistant.";
    public final static int defaultAiModel = 15;
    public final static double defaultTemperature = 0.7;
    public final static int defaultContextLimit = 30;
    public final static int defaultTokenLimit = -100;
    public final static String defaultCustomModel = "";
    public final static boolean defaultOldAgreement = false;

    public final static boolean defaultGeminiSafe = true;

    public final static boolean defaultStreamResponses = true;
    public final static boolean defaultRenderMarkdown = true;
    public final static boolean defaultAutoHideKeyboard = false;
    public final static boolean defaultSwitchSubtitleContent = false;

    public final static boolean defaultHideToolbar = false;

    public final static int defaultUserId = 2000;

    public final static String defaultApiServer = "https://api.openai.com/";
    public final static String defaultApiServerGoogle = "https://generativelanguage.googleapis.com/";
    public final static String defaultApiServerClaude = "https://api.claude.ai/";

    public int aiModel = defaultAiModel;
    public double temperature = defaultTemperature;
    public int contextLimit = defaultContextLimit;
    public int tokenLimit = defaultTokenLimit;
    public String customModel;
    public boolean isOldAgreement = defaultOldAgreement;
    public boolean isGeminiSafe = defaultGeminiSafe;

    public boolean isHideToolbar = defaultHideToolbar;

    public String apiKey;
    public String apiServer = defaultApiServer;

    public String apiKeyGoogle;
    public String apiServerGoogle = defaultApiServerGoogle;

    public String apiKeyClaude;
    public String apiServerClaude = defaultApiServerClaude;

    public boolean streamResponses = defaultStreamResponses;
    public boolean renderMarkdown = defaultRenderMarkdown;
    public boolean autoHideKeyboard = defaultAutoHideKeyboard;
    public boolean switchSubtitleContent = defaultSwitchSubtitleContent;


    private static volatile UserConfig[] Instance = new UserConfig[UserConfig.MAX_ACCOUNT_COUNT];
    public static UserConfig getInstance(int num) {
        UserConfig localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (UserConfig.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new UserConfig(num);
                }
            }
        }
        return localInstance;
    }

    public static int getActivatedAccountsCount() {
        int count = 0;
        for (int a = 0; a < MAX_ACCOUNT_COUNT; a++) {
            if (AccountInstance.getInstance(a).getUserConfig().isClientActivated()) {
                count++;
            }
        }
        return count;
    }

    public UserConfig(int instance) {
        super(instance);
    }

    public static boolean hasPremiumOnAccounts() {
        for (int a = 0; a < MAX_ACCOUNT_COUNT; a++) {
            if (AccountInstance.getInstance(a).getUserConfig().isClientActivated() && AccountInstance.getInstance(a).getUserConfig().getUserConfig().isPremium()) {
                return true;
            }
        }
        return false;
    }

    public static int getMaxAccountCount() {
        if (BuildVars.IS_CHAT_AIR) return 5;
        return hasPremiumOnAccounts() ? 5 : 3;
    }

    //本地默认聊天id，最终需要从服务器重新更新
    public int getNewMessageId() {
        int id;
        synchronized (sync) {
            id = lastSendMessageId;
            if (BuildVars.IS_CHAT_AIR) {

                if (lastSendMessageId <= 0) lastSendMessageId = 1000;
                lastSendMessageId++;

            } else {
                lastSendMessageId--;
            }
        }
        return id;
    }

    public int getNewUserId() {
        int id;
        synchronized (sync) {
            id = lastUserId;
            lastUserId++;
            if (id == 333000 || id == 777000 || id == 42777){
                return getNewUserId();
            }
        }
        return id;
    }

    public void saveConfig(boolean withFile) {
        NotificationCenter.getInstance(currentAccount).doOnIdle(() -> {
            if (!configLoaded) {
                return;
            }
            synchronized (sync) {
                try {
                    SharedPreferences.Editor editor = getPreferences().edit();
                    if (currentAccount == 0) {
                        editor.putInt("selectedAccount", selectedAccount);
                    }
                    editor.putBoolean("registeredForPush", registeredForPush);
                    editor.putInt("lastSendMessageId", lastSendMessageId);
                    editor.putInt("lastUserId", lastUserId);
                    editor.putInt("contactsSavedCount", contactsSavedCount);
                    editor.putInt("lastBroadcastId", lastBroadcastId);
                    editor.putInt("lastContactsSyncTime", lastContactsSyncTime);
                    editor.putInt("lastHintsSyncTime", lastHintsSyncTime);
                    editor.putBoolean("draftsLoaded", draftsLoaded);
                    editor.putBoolean("unreadDialogsLoaded", unreadDialogsLoaded);
                    editor.putInt("ratingLoadTime", ratingLoadTime);
                    editor.putInt("botRatingLoadTime", botRatingLoadTime);
                    editor.putBoolean("contactsReimported", contactsReimported);
                    editor.putInt("loginTime", loginTime);
                    editor.putBoolean("syncContacts", syncContacts);
                    editor.putBoolean("suggestContacts", suggestContacts);
                    editor.putBoolean("hasSecureData", hasSecureData);
                    editor.putBoolean("notificationsSettingsLoaded3", notificationsSettingsLoaded);
                    editor.putBoolean("notificationsSignUpSettingsLoaded", notificationsSignUpSettingsLoaded);
                    editor.putLong("autoDownloadConfigLoadTime", autoDownloadConfigLoadTime);
                    editor.putBoolean("hasValidDialogLoadIds", hasValidDialogLoadIds);
                    editor.putInt("sharingMyLocationUntil", sharingMyLocationUntil);
                    editor.putInt("lastMyLocationShareTime", lastMyLocationShareTime);
                    editor.putBoolean("filtersLoaded", filtersLoaded);
                    editor.putStringSet("awaitBillingProductIds", new HashSet<>(awaitBillingProductIds));
                    if (billingPaymentPurpose != null) {
                        SerializedData data = new SerializedData(billingPaymentPurpose.getObjectSize());
                        billingPaymentPurpose.serializeToStream(data);
                        editor.putString("billingPaymentPurpose", Base64.encodeToString(data.toByteArray(), Base64.DEFAULT));
                        data.cleanup();
                    } else {
                        editor.remove("billingPaymentPurpose");
                    }
                    editor.putString("premiumGiftsStickerPack", premiumGiftsStickerPack);
                    editor.putLong("lastUpdatedPremiumGiftsStickerPack", lastUpdatedPremiumGiftsStickerPack);

                    editor.putString("genericAnimationsStickerPack", genericAnimationsStickerPack);
                    editor.putLong("lastUpdatedGenericAnimations", lastUpdatedGenericAnimations);

                    editor.putInt("6migrateOffsetId", migrateOffsetId);
                    if (migrateOffsetId != -1) {
                        editor.putInt("6migrateOffsetDate", migrateOffsetDate);
                        editor.putLong("6migrateOffsetUserId", migrateOffsetUserId);
                        editor.putLong("6migrateOffsetChatId", migrateOffsetChatId);
                        editor.putLong("6migrateOffsetChannelId", migrateOffsetChannelId);
                        editor.putLong("6migrateOffsetAccess", migrateOffsetAccess);
                    }

                    if (BuildVars.IS_CHAT_AIR) {
                        editor.putInt("aiModel", aiModel);
                        editor.putString("temperature", String.valueOf(temperature));
                        editor.putInt("contextLimit", contextLimit);
                        editor.putInt("tokenLimit", tokenLimit);
                        editor.putString("customModel", customModel);
                        editor.putString("apiKey", apiKey);
                        editor.putString("apiServer", apiServer);
                        editor.putString("apiKeyGoogle", apiKeyGoogle);
                        editor.putString("apiServerGoogle", apiServerGoogle);
                        editor.putString("apiKeyClaude", apiKeyClaude);
                        editor.putString("apiServerClaude", apiServerClaude);
                        editor.putBoolean("streamResponses", streamResponses);
                        editor.putBoolean("renderMarkdown", renderMarkdown);
                        editor.putBoolean("autoHideKeyboard", autoHideKeyboard);
                        editor.putBoolean("switchSubtitleContent", switchSubtitleContent);
                        editor.putBoolean("oldAgreement", isOldAgreement);
                        editor.putBoolean("geminiSafe", isGeminiSafe);
                        editor.putBoolean("hideToolbar", isHideToolbar);
                    }

                    if (unacceptedTermsOfService != null) {
                        try {
                            SerializedData data = new SerializedData(unacceptedTermsOfService.getObjectSize());
                            unacceptedTermsOfService.serializeToStream(data);
                            editor.putString("terms", Base64.encodeToString(data.toByteArray(), Base64.DEFAULT));
                            data.cleanup();
                        } catch (Exception ignore) {

                        }
                    } else {
                        editor.remove("terms");
                    }

                    SharedConfig.saveConfig();

                    if (tmpPassword != null) {
                        SerializedData data = new SerializedData();
                        tmpPassword.serializeToStream(data);
                        String string = Base64.encodeToString(data.toByteArray(), Base64.DEFAULT);
                        editor.putString("tmpPassword", string);
                        data.cleanup();
                    } else {
                        editor.remove("tmpPassword");
                    }

                    if (currentUser != null) {
                        if (withFile) {
                            SerializedData data = new SerializedData();
                            currentUser.serializeToStream(data);
                            String string = Base64.encodeToString(data.toByteArray(), Base64.DEFAULT);
                            editor.putString("user", string);
                            data.cleanup();
                        }
                    } else {
                        editor.remove("user");
                    }

                    editor.commit();
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        });
    }

    public static boolean isValidAccount(int num) {
         return num >= 0 && num < UserConfig.MAX_ACCOUNT_COUNT && getInstance(num).isClientActivated();
    }

    public boolean isClientActivated() {
        synchronized (sync) {
            return currentUser != null;
        }
    }

    public long getClientUserId() {
        synchronized (sync) {
            return currentUser != null ? currentUser.id : 0;
        }
    }

    public String getClientPhone() {
        synchronized (sync) {
            return currentUser != null && currentUser.phone != null ? currentUser.phone : "";
        }
    }

    public TLRPC.User getCurrentUser() {
        synchronized (sync) {
            return currentUser;
        }
    }

    public void setCurrentUser(TLRPC.User user) {
        synchronized (sync) {
            TLRPC.User oldUser = currentUser;
            currentUser = user;
            clientUserId = user.id;
            checkPremiumSelf(oldUser, user);
        }
    }

    private void checkPremiumSelf(TLRPC.User oldUser, TLRPC.User newUser) {
        if (oldUser == null || (newUser != null && oldUser.premium != newUser.premium)) {
            AndroidUtilities.runOnUIThread(() -> {
                getMessagesController().updatePremium(newUser.premium);
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.currentUserPremiumStatusChanged);
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.premiumStatusChangedGlobal);

                getMediaDataController().loadPremiumPromo(false);
                getMediaDataController().loadReactions(false, true);
            });
        }
    }

    public void
    loadConfig() {
        synchronized (sync) {
            if (configLoaded) {
                return;
            }
            SharedPreferences preferences = getPreferences();
            if (currentAccount == 0) {
                selectedAccount = preferences.getInt("selectedAccount", 0);
            }
            registeredForPush = preferences.getBoolean("registeredForPush", false);
            lastSendMessageId = preferences.getInt("lastSendMessageId", !BuildVars.IS_CHAT_AIR ? -210000 : 1000);
            lastUserId = preferences.getInt("lastUserId", defaultUserId);
            contactsSavedCount = preferences.getInt("contactsSavedCount", 0);
            lastBroadcastId = preferences.getInt("lastBroadcastId", -1);
            lastContactsSyncTime = preferences.getInt("lastContactsSyncTime", (int) (System.currentTimeMillis() / 1000) - 23 * 60 * 60);
            lastHintsSyncTime = preferences.getInt("lastHintsSyncTime", (int) (System.currentTimeMillis() / 1000) - 25 * 60 * 60);
            draftsLoaded = preferences.getBoolean("draftsLoaded", false);
            unreadDialogsLoaded = preferences.getBoolean("unreadDialogsLoaded", false);
            contactsReimported = preferences.getBoolean("contactsReimported", false);
            ratingLoadTime = preferences.getInt("ratingLoadTime", 0);
            botRatingLoadTime = preferences.getInt("botRatingLoadTime", 0);
            loginTime = preferences.getInt("loginTime", currentAccount);
            syncContacts = preferences.getBoolean("syncContacts", BuildVars.IS_CHAT_AIR ? false : true);
            suggestContacts = preferences.getBoolean("suggestContacts", BuildVars.IS_CHAT_AIR ? false : true);
            hasSecureData = preferences.getBoolean("hasSecureData", false);
            notificationsSettingsLoaded = preferences.getBoolean("notificationsSettingsLoaded3", false);
            notificationsSignUpSettingsLoaded = preferences.getBoolean("notificationsSignUpSettingsLoaded", false);
            autoDownloadConfigLoadTime = preferences.getLong("autoDownloadConfigLoadTime", 0);
            hasValidDialogLoadIds = preferences.contains("2dialogsLoadOffsetId") || preferences.getBoolean("hasValidDialogLoadIds", false);
            sharingMyLocationUntil = preferences.getInt("sharingMyLocationUntil", 0);
            lastMyLocationShareTime = preferences.getInt("lastMyLocationShareTime", 0);
            filtersLoaded = preferences.getBoolean("filtersLoaded", false);
            awaitBillingProductIds = new ArrayList<>(preferences.getStringSet("awaitBillingProductIds", Collections.emptySet()));
            if (preferences.contains("billingPaymentPurpose")) {
                String purpose = preferences.getString("billingPaymentPurpose", null);
                if (purpose != null) {
                    byte[] arr = Base64.decode(purpose, Base64.DEFAULT);
                    if (arr != null) {
                        SerializedData data = new SerializedData();
                        billingPaymentPurpose = TLRPC.InputStorePaymentPurpose.TLdeserialize(data, data.readInt32(false), false);
                        data.cleanup();
                    }
                }
            }
            premiumGiftsStickerPack = preferences.getString("premiumGiftsStickerPack", null);
            lastUpdatedPremiumGiftsStickerPack = preferences.getLong("lastUpdatedPremiumGiftsStickerPack", 0);

            genericAnimationsStickerPack = preferences.getString("genericAnimationsStickerPack", null);
            lastUpdatedGenericAnimations = preferences.getLong("lastUpdatedGenericAnimations", 0);


            try {
                String terms = preferences.getString("terms", null);
                if (terms != null) {
                    byte[] arr = Base64.decode(terms, Base64.DEFAULT);
                    if (arr != null) {
                        SerializedData data = new SerializedData(arr);
                        unacceptedTermsOfService = TLRPC.TL_help_termsOfService.TLdeserialize(data, data.readInt32(false), false);
                        data.cleanup();
                    }
                }
            } catch (Exception e) {
                FileLog.e(e);
            }

            migrateOffsetId = preferences.getInt("6migrateOffsetId", 0);
            if (migrateOffsetId != -1) {
                migrateOffsetDate = preferences.getInt("6migrateOffsetDate", 0);
                migrateOffsetUserId = AndroidUtilities.getPrefIntOrLong(preferences, "6migrateOffsetUserId", 0);
                migrateOffsetChatId = AndroidUtilities.getPrefIntOrLong(preferences, "6migrateOffsetChatId", 0);
                migrateOffsetChannelId = AndroidUtilities.getPrefIntOrLong(preferences, "6migrateOffsetChannelId", 0);
                migrateOffsetAccess = preferences.getLong("6migrateOffsetAccess", 0);
            }

            String string = preferences.getString("tmpPassword", null);
            if (string != null) {
                byte[] bytes = Base64.decode(string, Base64.DEFAULT);
                if (bytes != null) {
                    SerializedData data = new SerializedData(bytes);
                    tmpPassword = TLRPC.TL_account_tmpPassword.TLdeserialize(data, data.readInt32(false), false);
                    data.cleanup();
                }
            }

            string = preferences.getString("user", null);
            if (string != null) {
                byte[] bytes = Base64.decode(string, Base64.DEFAULT);
                if (bytes != null) {
                    SerializedData data = new SerializedData(bytes);
                    currentUser = TLRPC.User.TLdeserialize(data, data.readInt32(false), false);
                    data.cleanup();
                }
            }
            if (currentUser != null) {
                checkPremiumSelf(null, currentUser);
                clientUserId = currentUser.id;
            }

            if (BuildVars.IS_CHAT_AIR) {
                initAiModelList();

                //默认配置
                aiModel = preferences.getInt("aiModel", defaultAiModel);
                temperature = Double.parseDouble(preferences.getString("temperature", String.valueOf(defaultTemperature)));
                contextLimit = preferences.getInt("contextLimit", defaultContextLimit);
                tokenLimit = preferences.getInt("tokenLimit", defaultTokenLimit);
                customModel = preferences.getString("customModel", defaultCustomModel);
                isOldAgreement = preferences.getBoolean("oldAgreement", defaultOldAgreement);
                isGeminiSafe = preferences.getBoolean("geminiSafe", defaultGeminiSafe);
                apiKey = preferences.getString("apiKey", "");
                apiServer = preferences.getString("apiServer", defaultApiServer);
                streamResponses = preferences.getBoolean("streamResponses", defaultStreamResponses);
                renderMarkdown = preferences.getBoolean("renderMarkdown", defaultRenderMarkdown);
                autoHideKeyboard = preferences.getBoolean("autoHideKeyboard", defaultAutoHideKeyboard);
                switchSubtitleContent = preferences.getBoolean("switchSubtitleContent", defaultSwitchSubtitleContent);
                isHideToolbar = preferences.getBoolean("hideToolbar", defaultHideToolbar);

                apiKeyGoogle = preferences.getString("apiKeyGoogle", "");
                apiServerGoogle = preferences.getString("apiServerGoogle", defaultApiServerGoogle);

                apiKeyClaude = preferences.getString("apiKeyClaude", "");
                apiServerClaude = preferences.getString("apiServerClaude", defaultApiServerClaude);
            }

            configLoaded = true;
        }
    }

    private void initAiModelList() {
        //根据不同接入方，载入不同的模型
        if (aiModelList != null) {
            aiModelList.clear();
        } else {
            aiModelList = new LinkedHashMap<>();
        }
        aiModelList.put(15, new AiModelBean("GPT-4o mini", "gpt-4o-mini", true));
        aiModelList.put(13, new AiModelBean("GPT-4o", "gpt-4o", true));
        aiModelList.put(17, new AiModelBean("GPT-4o-0806", "gpt-4o-2024-08-06", true));
        aiModelList.put(18, new AiModelBean("GPT-4o-0513", "gpt-4o-2024-05-13", true));
        aiModelList.put(19, new AiModelBean("o1 mini", "o1-mini", true));
        aiModelList.put(20, new AiModelBean("o1 preview", "o1-preview", true));
        aiModelList.put(21, new AiModelBean("o1 mini-2024-09-12", "o1-mini-2024-09-12", true));
        aiModelList.put(22, new AiModelBean("o1 preview-2024-09-12", "o1-preview-2024-09-12", true));
        aiModelList.put(1, new AiModelBean("GPT-3.5", "gpt-3.5-turbo", true));
        aiModelList.put(2, new AiModelBean("GPT-3.5-0613", "gpt-3.5-turbo-0613", false));
        aiModelList.put(3, new AiModelBean("GPT-3.5-16k", "gpt-3.5-turbo-16k", true));
        aiModelList.put(4, new AiModelBean("GPT-3.5-16k-0613", "gpt-3.5-turbo-16k-0613", false));
        aiModelList.put(12, new AiModelBean("GPT-3.5-0125", "gpt-3.5-turbo-0125", true));
        aiModelList.put(5, new AiModelBean("GPT-4", "gpt-4", true));
        aiModelList.put(16, new AiModelBean("GPT-4 Trubo", "gpt-4-turbo", true));
        aiModelList.put(6, new AiModelBean("GPT-4-0613", "gpt-4-0613", false));
        aiModelList.put(7, new AiModelBean("GPT-4-32k", "gpt-4-32k", true));
        aiModelList.put(8, new AiModelBean("GPT-4-32k-0613", "gpt-4-32k-0613", false));
        aiModelList.put(9, new AiModelBean("GPT-4-1106-preview", "gpt-4-1106-preview", false));
        aiModelList.put(11, new AiModelBean("GPT-4-0125-preview", "gpt-4-0125-preview", true));
        aiModelList.put(10, new AiModelBean("GPT-4-vision-preview", "gpt-4-vision-preview",
                "GPT-4-vision-preview (Picture model)", false));
        aiModelList.put(14, new AiModelBean("GPT-4o-Picture", "gpt-4o",
                "GPT-4o (Picture model)", false));
//        initOpenrouter();
        initGoogle();
        initClaude();
        aiModelList.put(0, new AiModelBean(LocaleController.getString("CustomModel", R.string.CustomModel), "custom Model", true));

    }

    //https://openrouter.ai/docs
    private void initOpenrouter() {
        if (aiModelList == null) return;
        aiModelList.put(101, new AiModelBean("openrouter/gpt-3.5-turbo", "openai/gpt-3.5-turbo", true));
        aiModelList.put(102, new AiModelBean("openroutergpt-3.5-turbo-0301", "openai/gpt-3.5-turbo-0301", true));
        aiModelList.put(103, new AiModelBean("openrouter/gpt-3.5-turbo-16k", "openai/gpt-3.5-turbo-16k", true));
        aiModelList.put(104, new AiModelBean("openrouter/gpt-4", "openai/gpt-4", true));
        aiModelList.put(105, new AiModelBean("openrouter/gpt-4-0314", "openai/gpt-4-0314", true));
        aiModelList.put(106, new AiModelBean("openrouter/gpt-4-32k", "openai/gpt-4-32k", true));
        aiModelList.put(107, new AiModelBean("openrouter/gpt-4-32k-0314", "openai/gpt-4-32k-0314", true));
        aiModelList.put(108, new AiModelBean("openrouter/anthropic/claude-2", "anthropic/claude-2", true));
        aiModelList.put(109, new AiModelBean("openrouter/anthropic/claude-instant-v1", "anthropic/claude-instant-v1", true));
        aiModelList.put(110, new AiModelBean("openrouter/anthropic/claude-instant-v1-100k", "anthropic/claude-instant-v1-100k", true));
        aiModelList.put(111, new AiModelBean("openrouter/google/palm-2-chat-bison", "google/palm-2-chat-bison", true));
        aiModelList.put(112, new AiModelBean("openrouter/google/palm-2-codechat-bison", "google/palm-2-codechat-bison", true));
        aiModelList.put(113, new AiModelBean("openrouter/meta-llama/llama-2-13b-chat", "meta-llama/llama-2-13b-chat", true));
        aiModelList.put(114, new AiModelBean("openrouter/meta-llama/llama-2-70b-chat", "meta-llama/llama-2-70b-chat", true));
        aiModelList.put(115, new AiModelBean("openrouter/meta-llama/codellama-34b-instruct", "meta-llama/codellama-34b-instruct", true));
        aiModelList.put(116, new AiModelBean("openrouter/nousresearch/nous-hermes-llama2-13b", "nousresearch/nous-hermes-llama2-13b", true));
        aiModelList.put(117, new AiModelBean("openrouter/mancer/weaver", "mancer/weaver", true));
        aiModelList.put(118, new AiModelBean("openrouter/gryphe/mythomax-L2-13b", "gryphe/mythomax-L2-13b", true));
        aiModelList.put(119, new AiModelBean("openrouter/jondurbin/airoboros-l2-70b-2.1", "jondurbin/airoboros-l2-70b-2.1", true));
    }

    // https://ai.google.dev/models/gemini
    public void initGoogle() {
        if (aiModelList == null) return;
        aiModelList.put(803, new AiModelBean("Gemini Pro 1.5", "gemini-1.5-pro-latest", true));
        aiModelList.put(811, new AiModelBean("Gemini Pro 1.5 002", "gemini-1.5-pro-002", true));
        aiModelList.put(814, new AiModelBean("Gemini 2.0 Flash exp", "gemini-2.0-flash-exp", true));
        aiModelList.put(804, new AiModelBean("Gemini 1.5 Flash", "gemini-1.5-flash-latest", true));
        aiModelList.put(812, new AiModelBean("Gemini 1.5 Flash 002", "gemini-1.5-flash-002", true));
        aiModelList.put(801, new AiModelBean("Gemini Pro 1.0", "gemini-pro", true));
        aiModelList.put(807, new AiModelBean("Gemini Pro 1.5 exp-0827", "gemini-1.5-pro-exp-0827", true));
        aiModelList.put(808, new AiModelBean("Gemini Pro 1.5 exp-0801", "gemini-1.5-pro-exp-0801", true));
        aiModelList.put(809, new AiModelBean("Gemini 1.5 Flash exp-0827", "gemini-1.5-flash-exp-0827", true));
        aiModelList.put(813, new AiModelBean("Gemini 1.5 Flash 8b exp-0924", "gemini-1.5-flash-8b-exp-0924", true));
        aiModelList.put(810, new AiModelBean("Gemini 1.5 Flash 8b exp-0827", "gemini-1.5-flash-8b-exp-0827", true));
        aiModelList.put(802, new AiModelBean("Gemini Pro false", "gemini-pro-vision",
                "Gemini Pro Vision (Picture model)", false));
        aiModelList.put(805, new AiModelBean("Gemini Pro 1.5 Picture", "gemini-1.5-pro-latest",
                "Gemini Pro 1.5 (Picture model)", false));
        aiModelList.put(806, new AiModelBean("Gemini 1.5 Flash Picture", "gemini-1.5-flash-latest",
                "Gemini 1.5 Flash (Picture model)", false));

    }

    public void initClaude() {
        if (aiModelList == null) return;
        aiModelList.put(901, new AiModelBean("Claude 3 haiku", "claude-3-haiku-20240307",
                true));
        aiModelList.put(902, new AiModelBean("Claude 3 opus", "claude-3-opus-20240229",
                true));
        aiModelList.put(903, new AiModelBean("Claude 3 sonnet", "claude-3-sonnet-20240229",
                true));
        aiModelList.put(904, new AiModelBean("Claude 3.5 sonnet", "claude-3-5-sonnet-20240620",
                true));

    }

    public static boolean isOpenAiMulti(String model) {
        if (!TextUtils.isEmpty(model)
                && (model.equals("gpt-4-vision-preview")
        )) {
            return true;
        }
        return false;
    }
    public static String getGoogleVersion(String model) {
        if (!TextUtils.isEmpty(model)
                && (model.equals("gemini-1.5-pro-latest")
                || model.equals("gemini-1.5-flash-latest")
                || model.equals("gemini-1.5-pro-exp-0827")
                || model.equals("gemini-1.5-pro-exp-0801")
                || model.equals("gemini-1.5-flash-exp-0827")
                || model.equals("gemini-1.5-pro-002")
                || model.equals("gemini-1.5-flash-002")
                || model.equals("gemini-1.5-flash-8b-exp-0924")
                || model.equals("gemini-2.0-flash-exp")
        )) {
            return "v1beta";
        }
        return "v1";
    }

    public String getAiModelName(int aiModel) {

        String aiModelName = "";
        LinkedHashMap<Integer, AiModelBean> aiModelList
                = UserConfig.getInstance(currentAccount).aiModelList;
        if (aiModelList != null && aiModelList.size() != 0) {
            if (aiModelList.containsKey(aiModel)) {
                if (aiModel == 0) {
                    // 自定义model
                    aiModelName = UserConfig.getInstance(currentAccount).customModel;
                } else {
                    AiModelBean bean = aiModelList.get(aiModel);
                    if (bean != null) {
                        aiModelName = bean.getName();
                    }
                }
            }
        }

        return aiModelName;

    }

    public String getAiModelReal(int aiModel, long userId) {

        String aiModelReal = "";
        LinkedHashMap<Integer, AiModelBean> aiModelList
                = UserConfig.getInstance(currentAccount).aiModelList;
        if (aiModelList != null && aiModelList.size() != 0) {
            if (aiModelList.containsKey(aiModel)) {
                if (aiModel == 0) {
                    // 自定义model
                    TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(userId);
                    if (user != null && (user.flags2 & MessagesController.UPDATE_MASK_CHAT_AIR_AI_CUSTOM_MODEL) != 0) {
                        // 聊天配置
                        aiModelReal = user.customModel;
                    } else {
                        // 系统默认
                        aiModelReal = UserConfig.getInstance(currentAccount).customModel;
                    }


                } else {
                    AiModelBean bean = aiModelList.get(aiModel);
                    if (bean != null) {
                        aiModelReal = bean.getAiModel();
                    }
                }
            }
        }

        return aiModelReal;

    }

    public boolean isDefaultGeminiProVision() {

        int aiModel = UserConfig.getInstance(currentAccount).aiModel;
        boolean isGeminiProVision = UserConfig.getInstance(currentAccount).isJudgeByModelGemini(aiModel);

        return isGeminiProVision;

    }

    public boolean isDefaultClaude() {

        int aiModel = UserConfig.getInstance(currentAccount).aiModel;
        boolean isClaudeProVision = UserConfig.getInstance(currentAccount).isJudgeByModelClaude(aiModel);

        return isClaudeProVision;

    }

    public boolean isDefaultVision() {

        int aiModel = UserConfig.getInstance(currentAccount).aiModel;
        boolean isGeminiProVision = UserConfig.getInstance(currentAccount).isJudgeByModelVision(aiModel);

        return isGeminiProVision;

    }

    public static int getUserAiModel(int currentAccount , long userId) {

        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(userId);

        return getUserAiModel(currentAccount, user);
    }

    /**
     * 如果设置了个人model，则返回，否则返回系统model
     * @param currentAccount
     * @param user
     * @return
     */
    public static int getUserAiModel(int currentAccount, TLRPC.User user) {

        int aiModel = -1;

        if (user == null) return aiModel;

        if ((user.flags2 & MessagesController.UPDATE_MASK_CHAT_AIR_AI_MODEL) != 0) {
            if (UserConfig.getInstance(currentAccount).aiModelList.containsKey(user.aiModel)) {
                aiModel = user.aiModel;
            } else {
                aiModel = UserConfig.getInstance(currentAccount).aiModel;
            }
        } else {
            aiModel = UserConfig.getInstance(currentAccount).aiModel;
        }

        return aiModel;

    }

    public static String getUserAiModelName(int currentAccount, long userId) {

        int aiModel = getUserAiModel(currentAccount, userId);

        String aiModelReal = UserConfig.getInstance(currentAccount).getAiModelName(aiModel);

        if (aiModelReal == null) return "";

        return aiModelReal;

    }

    public static String getUserAiModelReal(int currentAccount, long userId) {

        int aiModel = getUserAiModel(currentAccount, userId);

        String aiModelReal = UserConfig.getInstance(currentAccount).getAiModelReal(aiModel, userId);

        if (aiModelReal == null) return "";

        return aiModelReal;

    }

    public boolean isJudgeByModelClaude(int aiModel) {

        if (aiModel == 901) return true;
        if (aiModel == 902) return true;
        if (aiModel == 903) return true;
        if (aiModel == 904) return true;

        return false;
    }

    public static boolean isUserGemini(int currentAccount, long userId) {

        int aiModel= getUserAiModel(currentAccount, userId);
        return UserConfig.getInstance(currentAccount).isJudgeByModelGemini(aiModel);

    }

    public boolean isJudgeByModelGemini(int aiModel) {

        if (aiModel == 801) return true;
        if (aiModel == 802) return true;
        if (aiModel == 803) return true;
        if (aiModel == 804) return true;
        if (aiModel == 805) return true;
        if (aiModel == 806) return true;
        if (aiModel == 807) return true;
        if (aiModel == 808) return true;
        if (aiModel == 809) return true;
        if (aiModel == 810) return true;
        if (aiModel == 811) return true;
        if (aiModel == 812) return true;
        if (aiModel == 813) return true;
        if (aiModel == 814) return true;

        return false;
    }

    public static boolean isUserGeminiProVision(int currentAccount, TLRPC.User user) {

        int aiModel= getUserAiModel(currentAccount, user);
        return UserConfig.getInstance(currentAccount).isJudgeByModelGeminiProVision(aiModel);

    }
    public static boolean isUserGeminiProVision(int currentAccount, long userId) {

        int aiModel= getUserAiModel(currentAccount, userId);
        return UserConfig.getInstance(currentAccount).isJudgeByModelGeminiProVision(aiModel);

    }

    public static boolean isJudgeByModelO(int aiModel) {
        if (aiModel == 19) return true;
        if (aiModel == 20) return true;
        if (aiModel == 21) return true;
        if (aiModel == 22) return true;

        return false;
    }

    public boolean isJudgeByModelGeminiProVision(int aiModel) {

        if (aiModel == 802) return true;
        if (aiModel == 805) return true;
        if (aiModel == 806) return true;

        return false;
    }

    public boolean isJudgeByModelOpenAIVision(int aiModel) {

        if (aiModel == 10) return true;
        if (aiModel == 14) return true;

        return false;
    }

    public static boolean isSupportImageModel(int currentAccount, long userId) {

        int aiModel = getUserAiModel(currentAccount, userId);
        return isMultiCompletionRequest(currentAccount, userId) && !isJudgeByModelO(aiModel);
    }

    public static boolean isMultiCompletionRequest(int currentAccount, long userId) {

        int aiModel = getUserAiModel(currentAccount, userId);

        // OpenAI
        if (aiModel == 10
            || aiModel == 13
            || aiModel == 14
            || aiModel == 15
            || aiModel == 16
            || aiModel == 17
            || aiModel == 18
        ) {
            if(UserConfig.getInstance(currentAccount).isOldAgreement) return false;
            return true;
        }

        // Gemini
        if (aiModel == 802
                || aiModel == 803
                || aiModel == 804
                || aiModel == 805
                || aiModel == 806
                || aiModel == 807
                || aiModel == 808
                || aiModel == 809
                || aiModel == 810
                || aiModel == 811
                || aiModel == 812
                || aiModel == 813
                || aiModel == 814
        ) return true;

        // Claude
        if (aiModel == 901
                || aiModel == 902
                || aiModel == 903
                || aiModel == 904
        ) return true;

        // Custom model
        if (aiModel == 0) {
            if(UserConfig.getInstance(currentAccount).isOldAgreement) return false;
            return true;
        }

        return false;
    }

    // 兼容Gemini Pro Vision 模型不支持上下文
    public static boolean isGeminiProVision (int currentAccount, long userId) {
        int aiModel = getUserAiModel(currentAccount, userId);
        return aiModel == 802;
    }

    // 是否使用图片模型
    public boolean isJudgeByModelVision(int aiModel) {

        if (aiModel == 802) return true;
        if (aiModel == 805) return true;
        if (aiModel == 806) return true;
        if (aiModel == 10) return true;
        if (aiModel == 14) return true;

        return false;
    }

    public static String getUserAiPrompt(int currentAccount, long userId) {

        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(userId);

        return getUserAiPrompt(currentAccount, user);

    }

    public static String getUserAiPrompt(int currentAccount, TLRPC.User user) {
        String aiPrompt = "";

        if (user == null) return aiPrompt;


        if ((user.flags2 & MessagesController.UPDATE_MASK_CHAT_AIR_PROMPT) != 0
                && !TextUtils.isEmpty(user.prompt)) {
            aiPrompt = user.prompt;
        } else {
            //系统默认的prompt
        }

        return aiPrompt;
    }

    public boolean isConfigLoaded() {
        return configLoaded;
    }

    public void savePassword(byte[] hash, byte[] salted) {
        savedPasswordTime = SystemClock.elapsedRealtime();
        savedPasswordHash = hash;
        savedSaltedPassword = salted;
    }

    public void checkSavedPassword() {
        if (savedSaltedPassword == null && savedPasswordHash == null || Math.abs(SystemClock.elapsedRealtime() - savedPasswordTime) < 30 * 60 * 1000) {
            return;
        }
        resetSavedPassword();
    }

    public void resetSavedPassword() {
        savedPasswordTime = 0;
        if (savedPasswordHash != null) {
            Arrays.fill(savedPasswordHash, (byte) 0);
            savedPasswordHash = null;
        }
        if (savedSaltedPassword != null) {
            Arrays.fill(savedSaltedPassword, (byte) 0);
            savedSaltedPassword = null;
        }
    }

    public SharedPreferences getPreferences() {
        if (currentAccount == 0) {
            return ApplicationLoader.applicationContext.getSharedPreferences("userconfing", Context.MODE_PRIVATE);
        } else {
            return ApplicationLoader.applicationContext.getSharedPreferences("userconfig" + currentAccount, Context.MODE_PRIVATE);
        }
    }

    public LongSparseArray<SaveToGallerySettingsHelper.DialogException> getSaveGalleryExceptions(int type) {
        if (type == SharedConfig.SAVE_TO_GALLERY_FLAG_PEER) {
            if (userSaveGalleryExceptions == null) {
                userSaveGalleryExceptions = SaveToGallerySettingsHelper.loadExceptions(ApplicationLoader.applicationContext.getSharedPreferences(SaveToGallerySettingsHelper.USERS_PREF_NAME + "_" + currentAccount, Context.MODE_PRIVATE));
            }
            return userSaveGalleryExceptions;
        } else if (type == SharedConfig.SAVE_TO_GALLERY_FLAG_GROUP) {
            if (groupsSaveGalleryExceptions == null) {
                groupsSaveGalleryExceptions = SaveToGallerySettingsHelper.loadExceptions(ApplicationLoader.applicationContext.getSharedPreferences(SaveToGallerySettingsHelper.GROUPS_PREF_NAME + "_" + currentAccount, Context.MODE_PRIVATE));
            }
            return groupsSaveGalleryExceptions;
        } else  if (type == SharedConfig.SAVE_TO_GALLERY_FLAG_CHANNELS) {
            if (chanelSaveGalleryExceptions == null) {
                chanelSaveGalleryExceptions = SaveToGallerySettingsHelper.loadExceptions(ApplicationLoader.applicationContext.getSharedPreferences(SaveToGallerySettingsHelper.CHANNELS_PREF_NAME + "_" + currentAccount, Context.MODE_PRIVATE));
            }
            return chanelSaveGalleryExceptions;
        }
        return null;
    }

    public void updateSaveGalleryExceptions(int type, LongSparseArray<SaveToGallerySettingsHelper.DialogException> exceptions) {
        if (type == SharedConfig.SAVE_TO_GALLERY_FLAG_PEER) {
            userSaveGalleryExceptions = exceptions;
            SaveToGallerySettingsHelper.saveExceptions(
                    ApplicationLoader.applicationContext.getSharedPreferences(SaveToGallerySettingsHelper.USERS_PREF_NAME + "_" + currentAccount, Context.MODE_PRIVATE),
                    userSaveGalleryExceptions
            );
        } else if (type == SharedConfig.SAVE_TO_GALLERY_FLAG_GROUP) {
            groupsSaveGalleryExceptions = exceptions;
            SaveToGallerySettingsHelper.saveExceptions(
                    ApplicationLoader.applicationContext.getSharedPreferences(SaveToGallerySettingsHelper.GROUPS_PREF_NAME + "_" + currentAccount, Context.MODE_PRIVATE),
                    groupsSaveGalleryExceptions
            );
        } else  if (type == SharedConfig.SAVE_TO_GALLERY_FLAG_CHANNELS) {
            chanelSaveGalleryExceptions = exceptions;
            SaveToGallerySettingsHelper.saveExceptions(
                    ApplicationLoader.applicationContext.getSharedPreferences(SaveToGallerySettingsHelper.CHANNELS_PREF_NAME + "_" + currentAccount, Context.MODE_PRIVATE),
                    chanelSaveGalleryExceptions
            );
        }
    }

    public void clearConfig() {
        getPreferences().edit().clear().apply();

        sharingMyLocationUntil = 0;
        lastMyLocationShareTime = 0;
        currentUser = null;
        clientUserId = 0;
        registeredForPush = false;
        contactsSavedCount = 0;
        lastSendMessageId = !BuildVars.IS_CHAT_AIR ? -210000 : 1000;
        lastUserId = defaultUserId;
        lastBroadcastId = -1;
        notificationsSettingsLoaded = false;
        notificationsSignUpSettingsLoaded = false;
        migrateOffsetId = -1;
        migrateOffsetDate = -1;
        migrateOffsetUserId = -1;
        migrateOffsetChatId = -1;
        migrateOffsetChannelId = -1;
        migrateOffsetAccess = -1;
        ratingLoadTime = 0;
        botRatingLoadTime = 0;
        draftsLoaded = false;
        contactsReimported = true;
        syncContacts = true;
        suggestContacts = true;
        unreadDialogsLoaded = true;
        hasValidDialogLoadIds = true;
        unacceptedTermsOfService = null;
        filtersLoaded = false;
        hasSecureData = false;
        loginTime = (int) (System.currentTimeMillis() / 1000);
        lastContactsSyncTime = (int) (System.currentTimeMillis() / 1000) - 23 * 60 * 60;
        lastHintsSyncTime = (int) (System.currentTimeMillis() / 1000) - 25 * 60 * 60;
        resetSavedPassword();
        boolean hasActivated = false;
        for (int a = 0; a < MAX_ACCOUNT_COUNT; a++) {
            if (AccountInstance.getInstance(a).getUserConfig().isClientActivated()) {
                hasActivated = true;
                break;
            }
        }

        if (BuildVars.IS_CHAT_AIR) {
            if (aiModelList != null) {
                aiModelList.clear();
            }

            initAiModelList();

            aiModel = defaultAiModel;
            temperature = defaultTemperature;
            contextLimit = defaultContextLimit;
            tokenLimit = defaultTokenLimit;
            customModel = defaultCustomModel;
            isOldAgreement = defaultOldAgreement;
            isGeminiSafe = defaultGeminiSafe;
            isHideToolbar = defaultHideToolbar;
            apiKey = "";
            apiServer = defaultApiServer;
            apiKeyGoogle = "";
            apiServerGoogle = defaultApiServerGoogle;
            apiKeyClaude = "";
            apiServerClaude = defaultApiServerClaude;
        }

        if (!hasActivated) {
            SharedConfig.clearConfig();
        }
        saveConfig(true);
    }

    public boolean isPinnedDialogsLoaded(int folderId) {
        return getPreferences().getBoolean("2pinnedDialogsLoaded" + folderId, false);
    }

    public void setPinnedDialogsLoaded(int folderId, boolean loaded) {
        getPreferences().edit().putBoolean("2pinnedDialogsLoaded" + folderId, loaded).commit();
    }

    public void clearPinnedDialogsLoaded() {
        SharedPreferences.Editor editor = getPreferences().edit();
        for (String key : getPreferences().getAll().keySet()) {
            if (key.startsWith("2pinnedDialogsLoaded")) {
                editor.remove(key);
            }
        }
        editor.apply();
    }

    public static final int i_dialogsLoadOffsetId = 0;
    public static final int i_dialogsLoadOffsetDate = 1;
    public static final int i_dialogsLoadOffsetUserId = 2;
    public static final int i_dialogsLoadOffsetChatId = 3;
    public static final int i_dialogsLoadOffsetChannelId = 4;
    public static final int i_dialogsLoadOffsetAccess = 5;

    public int getTotalDialogsCount(int folderId) {
        return getPreferences().getInt("2totalDialogsLoadCount" + (folderId == 0 ? "" : folderId), 0);
    }

    public void setTotalDialogsCount(int folderId, int totalDialogsLoadCount) {
        getPreferences().edit().putInt("2totalDialogsLoadCount" + (folderId == 0 ? "" : folderId), totalDialogsLoadCount).commit();
    }

    public long[] getDialogLoadOffsets(int folderId) {
        SharedPreferences preferences = getPreferences();
        int dialogsLoadOffsetId = preferences.getInt("2dialogsLoadOffsetId" + (folderId == 0 ? "" : folderId), hasValidDialogLoadIds ? 0 : -1);
        int dialogsLoadOffsetDate = preferences.getInt("2dialogsLoadOffsetDate" + (folderId == 0 ? "" : folderId), hasValidDialogLoadIds ? 0 : -1);
        long dialogsLoadOffsetUserId = AndroidUtilities.getPrefIntOrLong(preferences, "2dialogsLoadOffsetUserId" + (folderId == 0 ? "" : folderId), hasValidDialogLoadIds ? 0 : -1);
        long dialogsLoadOffsetChatId = AndroidUtilities.getPrefIntOrLong(preferences, "2dialogsLoadOffsetChatId" + (folderId == 0 ? "" : folderId), hasValidDialogLoadIds ? 0 : -1);
        long dialogsLoadOffsetChannelId = AndroidUtilities.getPrefIntOrLong(preferences, "2dialogsLoadOffsetChannelId" + (folderId == 0 ? "" : folderId), hasValidDialogLoadIds ? 0 : -1);
        long dialogsLoadOffsetAccess = preferences.getLong("2dialogsLoadOffsetAccess" + (folderId == 0 ? "" : folderId), hasValidDialogLoadIds ? 0 : -1);
        return new long[]{dialogsLoadOffsetId, dialogsLoadOffsetDate, dialogsLoadOffsetUserId, dialogsLoadOffsetChatId, dialogsLoadOffsetChannelId, dialogsLoadOffsetAccess};
    }

    public void setDialogsLoadOffset(int folderId, int dialogsLoadOffsetId, int dialogsLoadOffsetDate, long dialogsLoadOffsetUserId, long dialogsLoadOffsetChatId, long dialogsLoadOffsetChannelId, long dialogsLoadOffsetAccess) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putInt("2dialogsLoadOffsetId" + (folderId == 0 ? "" : folderId), dialogsLoadOffsetId);//配置网络加载最后的id
        editor.putInt("2dialogsLoadOffsetDate" + (folderId == 0 ? "" : folderId), dialogsLoadOffsetDate);//配置网络加载最后的日期
        editor.putLong("2dialogsLoadOffsetUserId" + (folderId == 0 ? "" : folderId), dialogsLoadOffsetUserId);
        editor.putLong("2dialogsLoadOffsetChatId" + (folderId == 0 ? "" : folderId), dialogsLoadOffsetChatId);
        editor.putLong("2dialogsLoadOffsetChannelId" + (folderId == 0 ? "" : folderId), dialogsLoadOffsetChannelId);
        editor.putLong("2dialogsLoadOffsetAccess" + (folderId == 0 ? "" : folderId), dialogsLoadOffsetAccess);
        editor.putBoolean("hasValidDialogLoadIds", true);
        editor.commit();
    }

    public boolean isPremium() {
        if (currentUser == null) {
            return false;
        }
        return currentUser.premium;
    }

    public Long getEmojiStatus() {
        return UserObject.getEmojiStatusDocumentId(currentUser);
    }


    int globalTtl = 0;
    boolean ttlIsLoading = false;
    long lastLoadingTime;

    public int getGlobalTTl() {
        return globalTtl;
    }

    public void loadGlobalTTl() {
        if (ttlIsLoading || System.currentTimeMillis() - lastLoadingTime < 60 * 1000) {
            return;
        }
        ttlIsLoading = true;
        TLRPC.TL_messages_getDefaultHistoryTTL getDefaultHistoryTTL = new TLRPC.TL_messages_getDefaultHistoryTTL();
        getConnectionsManager().sendRequest(getDefaultHistoryTTL, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (response != null) {
                globalTtl = ((TLRPC.TL_defaultHistoryTTL) response).period / 60;
                getNotificationCenter().postNotificationName(NotificationCenter.didUpdateGlobalAutoDeleteTimer);
                ttlIsLoading = false;
                lastLoadingTime = System.currentTimeMillis();
            }
        }));

    }

    public void setGlobalTtl(int ttl) {
        globalTtl = ttl;
    }

    public void clearFilters() {
        getPreferences().edit().remove("filtersLoaded").apply();
        filtersLoaded = false;
    }
}
