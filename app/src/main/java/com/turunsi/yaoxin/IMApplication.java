// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin;

import static com.yaoxin.appbase.net.Constant.AccountDetailActivityKey;
import static com.yaoxin.appbase.net.Constant.RealName_Router;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import com.google.gson.Gson;
import com.heytap.msp.push.HeytapPushManager;
import com.huawei.hms.support.common.ActivityMgr;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RevokeMsgNotification;
import com.netease.nimlib.sdk.msg.model.ShowNotificationWhenRevokeFilter;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamMessageNotifyTypeEnum;
import com.netease.nimlib.sdk.team.model.IMMessageFilter;
import com.netease.yunxin.kit.chatkit.model.TeamWithCurrentMember;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.teamkit.ui.fun.activity.FunTeamMemberListActivity;
import com.netease.yunxin.kit.teamkit.ui.normal.activity.TeamMemberListActivity;
import com.orhanobut.hawk.Hawk;
import com.scwang.smart.refresh.footer.BallPulseFooter;
import com.scwang.smart.refresh.header.MaterialHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshFooter;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.DefaultRefreshFooterCreator;
import com.scwang.smart.refresh.layout.listener.DefaultRefreshHeaderCreator;
import com.tencent.bugly.crashreport.CrashReport;
import com.turunsi.yaoxin.crash.AppCrashHandler;
import com.turunsi.yaoxin.login.LoginActivity;
import com.turunsi.yaoxin.login.OtherPlaceLoginActivity;
import com.turunsi.yaoxin.login.RealNameSetActivity;
import com.turunsi.yaoxin.main.MainActivity;
import com.turunsi.yaoxin.main.conversation.XiaoZhuShouActivity;
import com.turunsi.yaoxin.main.mine.MineInfoActivity;
import com.turunsi.yaoxin.main.mine.account.AccountDetailActivity;
import com.turunsi.yaoxin.main.mine.collection.CollectionListActivity;
import com.turunsi.yaoxin.main.mine.fuhao.BuyGroupFeatureActivity;
import com.turunsi.yaoxin.push.PushMessageHandler;
import com.turunsi.yaoxin.register.ForgetPwdActivity;
import com.turunsi.yaoxin.register.RegisterActivity;
import com.turunsi.yaoxin.splash.SplashActivity;
import com.turunsi.yaoxin.utils.Constant;
import com.turunsi.yaoxin.utils.DataUtils;
import com.turunsi.yaoxin.welcome.WelcomeActivity;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.repo.SettingRepo;
import com.netease.yunxin.kit.corekit.im.utils.IMKitUtils;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.locationkit.LocationKitClient;
import com.vivo.push.PushClient;
import com.vivo.push.util.VivoPushException;
import com.yaoxin.appbase.activity.BaseWebViewActivity;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.DataUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IMApplication extends MultiDexApplication {

    private static final String TAG = "IMApplication";
    private static boolean coldStart = false;
    private static int foregroundActCount = 0;
    private Activity currentActivity;
    private MediaPlayer mediaPlayer;
    Vibrator vibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        ALog.d(Constant.PROJECT_TAG, TAG, "onCreate");
        // 云盾 SDK v5.4（clinkapi），密钥 ID 24017；返回 150 表示启动成功
        String sdkKey = "3n8AAJDkZ1DCXhEAAIZHX082au+HtRSSEQiPD21pcGNzU3+hsAV+N1HK1/4cve9bnZE72Bh2N+blsdqHVVfiWb/yppsHpa5JtjOdvNwUdVMYNHDbLZdK/BuXu/0q9eD5530/KRZTU/fi8SLk13oJ0DyGbyHlBysop6h/bumvH79Ro9Nf1Es8w9949zt1ZSSgjunBooa/FN9SF4Ft0uMOMtFhkzSlqfvvSqpAbA7vskGE3t7+7Xw5kI7zDL3JzvD6acRpE9Hv+eTt0FfafqbRYmM/6PIEDkRVKAKksZw9JXu5RANIhp5vk+T5ERwkXRHnIalZiuX89lqAE+rOSkdIr6vbIDqjiNrEzlQOCvwoMcnvvI/x4pqVvE2nx3KzoEmcsGFwSD3WQ5jNlLqQ7etdWOEgSMqqMp87oWWhBbishmKgBByJjJ6KzK3MI2Vyjqwkrai6ocM9EFzhoE5445peDgwWUU7sRPDc8JKiyJ76b+qyj5t1gylnjCIV1KTjB5FV8xoJJeQ4mEzFGQFek2s590gOVcd4aas8EbcZvPvg60J8Hg76yASFvVBGYuo1XKdtoguhCln1HzMZLyhpxDIc9DsqnFOEB1w1DgkBAN6AVdoNhXPqmTgkFyrxutyFxTmWGSQil3rkvyuSm1wKuXfegHPlOAlN8dcMNIUkCYadFq2/LSInhD3DK2EcZR98mDDacCE5nPunato8iZxDoxOY+P4KHmK6VCSSyfpwxwv9wBAb0coVJ8iktsaBX5kC4FWliUD6M3fvbQnyVjwy6OHrFNv+tQYsR2NJOI/r29nTanmKKm0us2LhpYDc6AtqNKi/18+THrrtRYUbwCIeFtSvaDkZsYJzMhfvKP773ygIdlJJrMluHHhfnCbv8i+665aE5vnNFbJdHHuSaHAgKMcpr01K12fUVQrjEYNX4HPQVV597HJB2ZvFn2+mWcduyYH9nKmmV4q4nfWQCH/FnwUzK4hHsBDUJ1wF83SzZ1V2PGW7XV5i0ZHfkg6o6Wql23waoeKtYKb1cSegrXnb9JQJWSU1f3OLoOf6nyElBvUYystByYfNWNebUEiAtkPtXJYz4AD+eOHrfxca1WmTe2z+ahfmHJT3qV04p7oJlsjBbDmoUVP2KFQopJNcIbpPb8sa2k4PpgIkJo9spAmFI6zoVh4cL6Nvmu8nIXPPnxK6vf1ZwR5kTNt4k6EBAaBlbpJP+3anPbHg8r7LTtSvxZGqhSMPSNLThI1MJKBluaB8sWV+ayS2aASGbZeZllEs+BtMfet1V2/rOTdr2S4o0VVfnXYCLRadB1vEyFd+628GZt8aImvFjkAhuAvzxEdD543DNRehOnEmCPxWxhYJkIwyCglNqo7NEnx1Qlygf1sTyb6mCMDB/M7Am1t6pp1f/KDYVmc8x4ol+AJi7l6FqZ0tkzWcZQoAeD9jtNf6r0qQiozk0+r7hqPujOe1gYwe9qH8OApKCYnCWcCRhLFUOyQXO5/vLtE9f2cOiwseO5FAdbSre0Dh278lhJox96b6voNPTQ5SiDOcKD5Ej6AUQBkHnaTGj9r7daNVWSnttDrqOb0/FS05xJdWlliaG3+gnNGnhtxxxwgI3zMuc23YlzHDcTHr8vlyannmbBA+nB3ZE2ensvrmm443W5+LhZ/E1ewl113fPtOoNcj8TIDeNMC3L55QH+Rsbc0/FBj8BDUYNk/zYKHywETSekOz7ijfLrtUKRQWZLYqqvqK5Ua/W77d0DE/znb7x1aH8CLbPnXOWdODpJ/wvwge7ieexU9j47dXPXqksS/heIxctfUa9KEuF5rriAzv0NBCcinXKUoOVoEyyhHNuCSozu1fp6I12RaN36puMEsKQjqAVcRQ3KnMNVwVgD+z5YyZyaMn4rLbMzEcc9nW65G6aQfoE+wkKmTKkCAp/BXGg+Hq5L5q/Z/KduIM2AfndF2AGF+ITwwLXTvG7IBUSqYBbZWLLpyQ5QAEmkC8uIZDIF6GACOGtyWektMpbMpnoh9n/eeJBp8LjK5gET5bffar/Y9v95LlUF1Xz9wpN2EntJmYDVFaBMeZk2+xnucTHF6D9F4XfYdRa2uruAmZpm35dS9MJVIehvtxWx5potStS0iVtzfi3VFs75Z1XbTC64XNxKw2Sz7ouX7yJGzRFArl43Tu3CCE3/LnKJM+OAmZ7lFnl2TjusR+V3Q49ISuYRLTm2lF1ZKhSwSCaE8QVr9IDmbzZEBZbh9EWEJoP+ZvGCAtkKNL+71x5PXUn2Ks7DTDY2PIpjobi9L6go9RvGuZI2qQoo2lfnv1vKiWkin6oAe9q4NT0Oyf5L3+sVXZZ4u3+y5b50oERkN4H0QxyrDDZpOomWqzj+sHFqCYZKlzEx2pCFrqZBOoBJ9rGJJ4EfVTikrLfAvq2J4uHqWS4uSBh6/zEC2vqCaOcc7hbV1brcF2OHrSe3tQ/5/XV1qKf2dP1SgIb2pLLWmtd72aRjHkqMLPi11w8lXAcfj/z86NHSNCl+EOPgK7fWTlPmukWAOVVv/aPvHeTdd3FpF7yw3d/a+omBOiGWdHJ87AVtrwJGpqQtUxC4FpPj9a1JbEYu/y1iDGLNyypkcDYFfuc4qVbZG4y0lR5fS5wB26d0V+8PoLL33HtZh8LK4lEsGhLDmvtbwVnuB91p11EEi/KP6n2mtj/W79L5aZ7xs0uN/pDYVWGuxefOOM9umPnbztVAI1w0mELRMcyd1QfTE5I//mW015gFv37ajae8DWDGMK99qayzwetq2LIJHM5ztWgHsrlO/JmSW5DDXXvLznEn5HKuMwmiMCzvse0gYTtiLUQ1KiM6CoAW2RA3doofW8+cQXt3jVTZDi3Dr1w2ikXw1wY3l4x+sTTskumnFwDGW7xfZQj/S3eXMUBndnoIx9V0wPw8RKSmqf7uVdsn2rO5hLmj+/OK7VNeInfHEL3MtbXNZkWOEIUypx8AmdYqE823cImiAnjpnoQaMmavSdHjEmxxY7xXPpnYn3FOWQ0883UgeKbHenJ90lC/DOMOt8ifMJVUUa94UIfDIoJ+Y2MuSRlj37BVk3ztsO+mf6mHAs8Mv4IqgOprBOLR17z5SlbSy2fi0NTl2iBHosQ3kyxHi5nd+s9ZFQbw8r0J7xKlVW3UVBVG3fSi1OQeY66EbwExUuv/cI23mAEZK5q46Fzxu9UObdG4SZnEfNuH5kNpsDx2spnSmGmKwTD4oKl3nDa/JusE10xR1iSHvAZLC4Ld7Vi6qnG6fgojaSfTCcynfBSyzPhRB0sfOEZBweGUXzrxjoDFGsssNXT7cvIhJv83XHZRLkMy6GIWBubv5i2uIzsDrDxWUjKw4tE6nacFD7I51poyOXC8dJjSkcoEOWmeszo/LUaVbKz3sNBsPyskKIM69k20StZ6NOKqOxmQ6C2/sH2aC+CBrDAUUMhNxMwRUTP5QcdTkmzsWX/cqg0S2vVb8ZSWdyJJwqw5nNiSZw0fSn0/nK0SDUIGgAPcMnoUwAa8j4gqrvvhoAYB7AZNHnpDzmv7CsHZtAXD/Ew7qMaKumRFp+eiew+789eubWJ/dQ5DJeYJXdZmhbmVFogGECWw9BoMyTph2CnzYbIC/KaaKPBf2equ4MUEZ+NliL0EdjrQKNzjWVJAkZbYTGLEAl30ERRR3ei7zLR3dcvyco/5WFTZBdd6yz0El77E+5U29MKMqcM1YYasZTF7UfyYZh2a3dKFl4EhF/alHP1THxzePJE+xFTszi83qsQJUWBsmESV72PiJwyygqCGoyfVRELJ0PSGYMVax1dWHTMKjVX77icbYUdWwxYraQJKTlBVy6PH/bohDYozhWH+JoQIEnh9e6x/+8DlJNn0MqCpgJC4EAuFqDXIcVLFUvan1SYh/npA5gWYsq1PsJntetkYSV4ckaV30LIi7uxqGTxM3jH1s1ldZGug0Ndir9O+3oKyn0zTK2gFaqjXX1E5ThyKuot1H1fgffJa/LjegorieZwNcVjz08ypTj6VdaMmegH0BsmZHMQVGQwPxt52QtzUatjxNx9wUdiFJvhekLzps6GooGWg2C0U45rncRgWy5462cUMm9COXK9Ea80/tBuZdXYA3pGpSHaRUfHA3oOsu2qHsAhjyDjfJrb3wlF/f06OLPlwJEPGzL+p6CtnRf62uWEUXm5wfc6aVcQ1ju5MFaQ3BWnxdIjJhLIQAoU0rFIVh/yDORj9XtsOjJ9IBjGe9eqBOHSot4lFxjuNi48FNXVGnmwOjQL5Uy0eGwEO9BZeJ1sFKxqai6zEGTZ1jBWpxGtxoT3C0X6TAFM4zderp7oMKLJBTwt3pUW8RgdOkXUZxls6D7u7PYBUfzEvczEXxKe3Qy526lhn1nYW+4HKPVwHKHentzsM4agiTOa9gcYnQbLu570oOiWsraO4UF76uVfO+jImq7HjY6eMNgBG9QkiPnWMJA1jNleZNJIeqr/ZQhK1Mm7BkyCai7ijwU7FDkyncja9/yjyRjYvvcnMCsbb4wUZShliEnjPQAUZFhOlq1PagrC24BrM4Mj2sxVTugiIlqhm8e5lPyDiIkM+2jP7YRYxV83KngUrFZ0SJbfY8RmcrOcQWixs2Zi7gNJp53gWhQ3Ke6xD7jEYq/8322JrVVeWptq46GFeQyDfrVQTPamSQrcFBNLjh1F0tHexHXmBFFw06Uol1xe3qFTjZbTphHtl8XYl/yRbd//W3xFtQn5IrwnamKs4Zu249+VShVuAnljmltmSUo/r5CEXHCg8+OJWIDMBpTjbC9O7DStV9XafaPbEkRzs9iZMz+lWLIIAzOdmkByq6PAFHGWFamcY0C3bO7cu9u1c9YRHkcFWZywzrA8J6IN0JbA9cYQLDV5DQ2PRzUGd3JFHhpVAeKNGvMIIblr4AbBp5p8MWK/g5A0Fqh2CEtzawOt7Y05BWa93y4dHRQBQtLkj69iD1QHD/7BfWbJB59VmSv7r7AggogworPFURUV6uUXr9SFRHz7/+5WBdU/yBszOpOskG3rsVLAPQQ0iHZgMkgcjcHAfSluev58C+WxoH7RbcyimdS7fKCoBMDaHhNN1VZMKQfbd8mFatwEJIxnEifv2WpleCer4QtP0VkT5HjNdJrgWalviGt3mkcFR3NidsaBFwqZO52rv5oCDWeEcb6fL51GFlYelbvMlhFTezB5AiUv2OLmuOdsIOWgS2W4+fAPuAPc9ZDmtwn9GG9KSo6/Ky4UxU+vu3wzZfgUvKwS3uCmssTOFeev8bnHTxZzLWCS1YcH794eCobEI//AknC+Zko0XbAq5Y/UhkaxOVbNVRbLOdZs60fHqo5V0B4A51kvptrimSYyLptMKa1n795up1g52s2/4iti5dLjqXRN4o08szet8LK+sv1zNt5DfRSMrP4JvXOTj/N0nt9SGR+7v1Po671M61rQGrGl30ylEmZePW7pfAeYa34buoG94TgFllV9h6nhXWUw1WKT7QONK2BuGhUEc0DLTzALjj+MHQxjZWRiBiwFzKGtWu7CxNMxFr+moVFOkjTCJj+W875fZkB3YdUxiE3ucoZtYPdcOzUClJAfgc4uWvxc/q8fEMNCmxRXclQqcYR6SpMcLk1n4gXADR8PjZb+n8dpp/wa5XejbZVyxBFYuv7wGgRLgtxWJq354wZePYjTt8EzmFJbo+SwbrkW5RfAtnpR9ZlgouFe5LCoPiTxBS/MnwwQZOKsVHXfmxPjPwGV+WyLjEfvMtTfVPNhs2W/fulJCgXL8BGX5SFjQQfNu2Vu0gS4Jbin3Sw28I/vCuDQX9wqGfT7Sgla86bmS/GNEdkOMmEgx1kwoSzZlL9BqFoAkuErYyGwM0g7HTHRgZE6KZ62oNh/bxhC+TiIIbVJ2QsMpfiQoW53Xb6ZJWnnHt+bnyqgisdBwOOIqKmo59HScUPMgEAAFAPAACcCQAAAFAAAABQAAAAUAAAARYAADE5NmJmNDEzZTNmMDRkYjkxOThlNzNkMGM0MTM0MGJmYmE2YzM1MTkyZDdlZTExNuoDAABcAAAA+AoAAG8IAADzDQAArw4AAJQCAAD7CgAAjAgAALkOAAAmCAAA5w4AAJgJAADBCwAAIwQAAOAJAAB4CwAAxgMAAHoOAABODgAALgEAAPsJAADCCgAAWgAAAE4NAADLBgAAAggAAKYMAAAhBAAA7Q0AAG4MAADDBgAARwMAAEcOAAAoDgAAqgAAALwGAABoAAAARgQAAEwKAABdCQAAaQcAAC0DAADiBgAAWAMAAOUKAADDDgAAawsAAMkDAADaDQAAfw0AAOoHAABjAwAA9gUAAD0EAAAwCQAA3AQAAJ0KAABiCgAAJwcAAKwGAAAwBQAA2gYAAOEJAABLAgAAYgEAAJ8LAACSCgAA2gwAAHkDAAAkAwAADwsAAMEOAAAtBAAA8QQAAAYJAADOBgAA3wMAAEQMAABpAgAASgcAANgLAADcCAAAiAUAAG0AAACEBAAAYAMAAJIMAAAKCgAA+AQAABALAABgCwAARwUAAGEMAACgAwAALAQAAI8AAACNCAAAAAAAAAAA7lmFpcn1BvX4GcaWyP1cL1uKyGNIPols6qWndZF//LarcNdIAzIjH55725O4Gm+/qjr+brHAmq/OGbojTZBjeRwP6lclFCwpZqjEy2H1ss815spXf40E6tQneOl2ANAOF2J3qC8YmZUO1AcJ+s5N2GXq7GXEDPiNC/6C/JpBsmbWtrP+fQqfHU00SKmF+u4G2J+EihJqSApo6JwwlJv6zCKgMkQaTlpaS84YkMNOCBAQeyP6uydTa22plZRsb4iwOXuH48ip64pEKj9CUrdGGuCMHxh3ABD5TcXVb7fE7NtGZ1dHxSNGZY+YtZHmEBm1Ynuz3k9vODlrjJYC0nvSfjxj++pf/VO8RbmI3kl9Ej2sHw08EQ2M1i4e2ziMRJEUxDdUgxQtVmMn9ZiU5lnu7HpZVLNulW0u1iS8QSEaMAEndBkSKI4ofsioFBtz9u7ULu69Mu7dpC7JJqvucBRCDMSXbXI/3MPqMNTpxYFRud815hMyXPwRSc7pSALhOQ60ZghV24l8lFAr0gIZ8t6sd2vLuYgAD6/GhSOjVJTACfXGNV54+91Vt1LBB4j691XuPM+WFX+Es1ZXjVVl2EC7ackMUb1/NHqMdQE2Pggmy43NsxfDKfJ1QoAIIW2H794SBpy+7aWWbghVCp5+TnVNMMHHliQo9j/VlQ89dxINBIUiJDdqZtznN0QObh9T9vMLue+rv1i56A5c6FiVWfb1RwUZWyk94sjj3l/eGYgggp0bMrwhYUQsZzooDX2EUtxNGCYaOZvJzNYmxt/32c6FPGW6eSc50+W/6/PtFNRKX4yCkQFhBUgCUWTl+ppjM4xrotAYyde1E0RAWUym2rcC2mZTdUjNIzcdtyjBU9hLXLdmgnEEMpW0y328Iav6WnWs4x8zEL1EnX+Z+RGu879q9PyUQFhG/KjQnanKEAKKIV2yFG65F0z5IuPwajeEBte8USuuZhocZuV1YXEUeIx8Dtiy+t5QsNyvmR1zVJJwTJfQDb7W27lDlq2IQ7XZ5ahXc1te0Ar4bJ9N/cz8pxFSBRXYQmvNpTEeb2Qwx20n1LJzHN83EnqM16AeiN3sRu2cNT2fAaI9ic0pP2GlyKMxJSgZiIsxG/KaS2rKrh1TMSl2k5Iwh9MQNpISDdEZ5g/762K007xhahpJBbQ4ctmv1A44LjQRAZ1UCVL3Hg0hgGNbzcG5nlxrFCh45YEe1sO3YTfD37fZ6urDYMpqaEsMKINQ3KMY9TawyxoMmBscNTGOQLzVXJ+rgeHW9T2RAarbbLz8zfkfn2sBEhSdzFLOpyuY2yiONcripFvBQf73HspuLyxeHmY3b6BQ1EqLDbOPB00S+CWCTTbwGiieXV1rCPJJLp0lcIjDYWyHPWCAV5amMTmx5SWk6+H1bAQ+mBFC5MtWQt4WaZbETZe4+Q/Ub8GQ9iskuSLNo0G6LwOEhchaRFiUKhW3kMeh65eN6/DSIPtBrKDlHiqoiJ2cWjmTQMfNXrW1QEWs2GVjgcyKP7ZP5g8GQtgncyYt6qF2GjWNDqvOvo07HN61lNM6gpxcHkjlTXMpjPzupumPiFWjNb0w1HIhu4ApFMovT60F7PCIz8tYkMzBY4Ct3hijq5OJEl4VCqwpO2LHvDin+T1P6bj4BreISqTwbXDxcttnhWg2pD1O7uJDX5RpkNKTGWlDXfReqaTbAca4JJs8NseGXzbPcFT+AfnVtKUiHrnQXFMHrRXlCdrCWqN2lcpGByjL4jGbwqcUQ1z5h/T+uOEM5sZHNYiyn1ZbDRawiGovj618VjuINJiq8Wq364Dlw6DAaa0D/dGfGUPaQLWRY7nwyvZGgWKmg7EunEId+bPUdEl07h+8K5Db3Dz2rPPhGY2vxiCIn0I+GuT71GGOgh6PBcpsHXSaNJ5laqMlrtW9S4K0bDOSS9QGNtM6v1YvTVoeZ6YodfsYFZGBIjWdGGoOPojd2RXp+7n0ZeagX3NINhxJB0YulNSGm1ehESifaB1oK4HMUXNFM2rsMhAVd7QdOesYUxEj3qizVj28SOqRVfvINzTweuSoBNPaQWLEcSShg9Y7hxQMRUzYGfk1h+l/W9wTS0CAK70Hbk89R5xG7eMZGsQUVcznvPHmGGHpc3voW+WEsSbiIVcZNBnEzfyukioVHGwlawUPKo1OmRNcHPnSU+u2wDu9Nfht+SCzIu3zN4U21FYOw+/fdIaXlIdEuxce8XoR99DuVuVzt3DkWwieWWGNJkejw4/61kuzJaBwKT4JlC9z4j+zR31vmgx/+hl++DomtZXPei7pFSCEOjFFAo4y2sbXKnUh2N7JMyiDetoyNSxlSnKnOBoBicweLgxMKpd9TR8=";
        cn.ay.clinkapi.Api clinkApi = new cn.ay.clinkapi.Api();
        int result = clinkApi.start(sdkKey);
        Log.d(TAG, "clinkapi start result=" + result);

        //app init
        registerActivityLifeCycle();
        AppCrashHandler.getInstance().initCrashHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(AppCrashHandler.getInstance());

        initUIKit();
        // temp register for mine
        XKitRouter.registerRouter(RouterConstant.PATH_MINE_INFO_PAGE, MineInfoActivity.class);
        XKitRouter.registerRouter(AccountDetailActivityKey, AccountDetailActivity.class);
        XKitRouter.registerRouter(RealName_Router, RealNameSetActivity.class);
        XKitRouter.registerRouter(com.yaoxin.appbase.net.Constant.TeamMemberListActivity_Router, FunTeamMemberListActivity.class);
        XKitRouter.registerRouter(com.yaoxin.appbase.net.Constant.CollectionListActivityKey, CollectionListActivity.class);
        XKitRouter.registerRouter(com.yaoxin.appbase.net.Constant.BaseWebViewActivityKey, BaseWebViewActivity.class);
        XKitRouter.registerRouter(com.yaoxin.appbase.net.Constant.XiaoZhuShouActivityKey, XiaoZhuShouActivity.class);
        XKitRouter.registerRouter("BuyGroupFeatureActivity", BuyGroupFeatureActivity.class);
        AppProxy.getInstance().init(this)
                .setIsDebug(BuildConfig.DEBUG)
                .setVersionName(BuildConfig.VERSION_NAME);
        initThirdPart();
        mediaPlayer = MediaPlayer.create(this, R.raw.msg);
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator()) { // 检查设备是否支持震动
            // 创建一个震动效果，参数为震动时长（毫秒）
            VibrationEffect vibrationEffect = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE);
                vibrator.vibrate(vibrationEffect);
            }
            // 开始震动
        }
    }


    private void initThirdPart() {
        Hawk.init(this).build();
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @NonNull
            @Override
            public RefreshHeader createRefreshHeader(@NonNull Context context, @NonNull RefreshLayout layout) {
                layout.setEnableHeaderTranslationContent(false);////是否下拉Header的时候向下平移列表或者内容
                return new MaterialHeader(context).setColorSchemeColors(getResources().getColor(R.color.color_884EEF));//Material design 风格Header
            }
        });


        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @NonNull
            @Override
            public RefreshFooter createRefreshFooter(@NonNull Context context, @NonNull RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                return new BallPulseFooter(context);
            }
        });
    }

    private void initUIKit() {
        SDKOptions options = NimSDKOptionConfig.getSDKOptions(this, DataUtils.readAppKey(this));
        options.teamNotificationMessageMarkUnread = true;
        IMKitClient.init(this, null, options);
        ALog.d(Constant.PROJECT_TAG, TAG, "initUIKit");

        if (IMKitUtils.isMainProcess(this)) {
            ALog.d(Constant.PROJECT_TAG, TAG, "initUIKit:isMainProcess");
            LocationKitClient.init(this);
            //huawei push
            ActivityMgr.INST.init(this);
            //oppo push
            HeytapPushManager.init(this, true);
            try {
                //vivo push
                PushClient.getInstance(this).initialize();
            } catch (VivoPushException e) {
                e.printStackTrace();
            }
            IMKitClient.toggleNotification(SettingRepo.isPushNotify());
            IMKitClient.registerMixPushMessageHandler(new PushMessageHandler());
            // 在 Application启动时注册，保证漫游、离线消息也能够回调此过滤器进行过滤。注意，过滤器的实现不要有耗时操作。
            NIMClient.getService(MsgServiceObserve.class)
                    .observeCustomNotification(new Observer<CustomNotification>() {
                        @Override
                        public void onEvent(CustomNotification notification) {
                            // 处理接收到的自定义系统通知
                            String content = notification.getContent();
                            if (content != null) {
                                CustomMsgBean msgBean = new Gson().fromJson(content, CustomMsgBean.class);
                                if (msgBean.type == 525) {
                                    BaseEvent baseEvent = new BaseEvent("egg_open_notice");
                                    baseEvent.customMsgBean = msgBean;
                                    EventBus.getDefault().post(baseEvent);
                                } else if (msgBean.type == 18) {
                                    BaseEvent baseEvent = new BaseEvent("login_out");
                                    EventBus.getDefault().post(baseEvent);
                                } else if (msgBean.type == 15 || msgBean.type == 2) {
                                    BaseEvent baseEvent = new BaseEvent("refreshFriendList");
                                    EventBus.getDefault().post(baseEvent);
                                }
                            }
                            // 根据需要处理通知内容
                        }
                    }, true);
            NIMClient.getService(MsgService.class).registerIMMessageFilter(new IMMessageFilter() {
                @Override
                public boolean shouldIgnore(IMMessage message) {
//                    if (message.getAttachStr() != null) {
//                        try {
//                            CustomMsgBean msgBean = new Gson().fromJson(message.getAttachStr(), CustomMsgBean.class);
//                            msgBean.result = new Gson().fromJson(msgBean.data, CustomMsgBean.class);
//                            if (msgBean.type == 21) {
//                                if (DataUtil.getUserid().equals(msgBean.result.toUserId) || DataUtil.getUserid().equals(msgBean.result.fromUserId) || msgBean.result.adminIds.contains(DataUtil.getUserid())) {
//                                    return false;
//                                }
//                                return true;
//
//                            }
//
//                        } catch (Exception e) {
//
//                        }
//                    }

                    if (message.getContent() != null && message.getContent().startsWith("{")) {
                        try {
                            CustomMsgBean msgBean = new Gson().fromJson(message.getContent(), CustomMsgBean.class);

                            if (msgBean.sendUserId != null && msgBean.sendUserName != null && msgBean.receiveUserName != null && msgBean.receiveUserId != null) {
                                {
                                    if (msgBean.sendUserId.equals(DataUtil.getUserid()) || msgBean.receiveUserId.equals(DataUtil.getUserid())) {
                                        return false;
                                    }
                                    return true;

                                }
                            }

                        } catch (Exception e) {

                        }
//                        if (message.getContent().contains("receiveUserId") && message.getContent().contains("receiveUserName")) {
//                            return true;
//                        }
                    }
                    String ringSoud = DataUtil.getStringValue("ring_soud");
                    String shakeSoud = DataUtil.getStringValue("shake_soud");
                    if ("1".equals(ringSoud)) {
                        // 获取会话 ID 和类型
                        String sessionId = message.getSessionId();
                        SessionTypeEnum sessionType = message.getSessionType();

                        if (sessionType == SessionTypeEnum.P2P) { // 单聊会话

                            boolean isNotice = ConversationRepo.isNotify(sessionId, SessionTypeEnum.P2P);

                            if (isNotice) {
                                mediaPlayer.start();
                            }
                        } else if (sessionType == SessionTypeEnum.Team) { // 群聊会话
                            TeamRepo.queryTeamWithMember(
                                    sessionId,
                                    Objects.requireNonNull(IMKitClient.account()),
                                    new FetchCallback<TeamWithCurrentMember>() {
                                        @Override
                                        public void onSuccess(@Nullable TeamWithCurrentMember param) {
                                            boolean isMute = param.getTeam().getMessageNotifyType() == TeamMessageNotifyTypeEnum.Mute;
                                            if (!isMute) {
                                                mediaPlayer.start();
                                            }
                                        }

                                        @Override
                                        public void onFailed(int code) {
                                        }

                                        @Override
                                        public void onException(@Nullable Throwable exception) {
                                        }
                                    });
                        }
                    }
                    if ("1".equals(shakeSoud)) {
                        // 获取会话 ID 和类型
                        String sessionId = message.getSessionId();
                        SessionTypeEnum sessionType = message.getSessionType();

                        if (sessionType == SessionTypeEnum.P2P) { // 单聊会话

                            boolean isNotice = ConversationRepo.isNotify(sessionId, SessionTypeEnum.P2P);

                            if (isNotice) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                                } else {
                                    vibrator.vibrate(500); // 简单震动
                                }
                            }
                        } else if (sessionType == SessionTypeEnum.Team) { // 群聊会话
                            TeamRepo.queryTeamWithMember(
                                    sessionId,
                                    Objects.requireNonNull(IMKitClient.account()),
                                    new FetchCallback<TeamWithCurrentMember>() {
                                        @Override
                                        public void onSuccess(@Nullable TeamWithCurrentMember param) {
                                            boolean isMute = param.getTeam().getMessageNotifyType() == TeamMessageNotifyTypeEnum.Mute;
                                            if (!isMute) {

                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                                                } else {
                                                    vibrator.vibrate(500); // 简单震动
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailed(int code) {
                                        }

                                        @Override
                                        public void onException(@Nullable Throwable exception) {
                                        }
                                    });
                        }


                    }
                    return false; // 不过滤
                }
            });

            CrashReport.initCrashReport(getApplicationContext(), "0fd2ddb379", false);
        }
    }

    private final List<Activity> activities = new ArrayList<>();

    //用于系统杀死应用之后，系统恢复应用，可能存在没有登录的异常
    //此处如果在没有登录的情况下，其他页面打开的时候进行finish();除了MainActivity
    //MainActivity启动进行登录检测，如果没有登录进行登录操作
    private void registerActivityLifeCycle() {
        registerActivityLifecycleCallbacks(
                new ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                        if (TextUtils.isEmpty(IMKitClient.account())
                                && !(activity instanceof MainActivity || activity instanceof SplashActivity || activity instanceof LoginActivity || activity instanceof RegisterActivity || activity instanceof ForgetPwdActivity || activity instanceof WelcomeActivity || activity instanceof RealNameSetActivity || activity instanceof BaseWebViewActivity || activity instanceof OtherPlaceLoginActivity)
                                && !coldStart) {
                            activity.finish();
                        } else {
                            activities.add(activity);
                        }
                    }

                    @Override
                    public void onActivityStarted(Activity activity) {
                        foregroundActCount++;
                        currentActivity = activity;
                    }

                    @Override
                    public void onActivityResumed(Activity activity) {
                        currentActivity = activity;
                    }

                    @Override
                    public void onActivityPaused(Activity activity) {
                    }

                    @Override
                    public void onActivityStopped(Activity activity) {
                        if (currentActivity == activity) {
                            currentActivity = null;
                        }
                        foregroundActCount--;
                    }

                    @Override
                    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                    }

                    @Override
                    public void onActivityDestroyed(Activity activity) {
                        if (activities.isEmpty()) {
                            return;
                        }
                        activities.remove(activity);
                    }
                });
    }

    public void clearActivity(Activity exclude) {
        for (int i = 0; i < activities.size(); i++) {
            if (activities.get(i) != null && activities.get(i) != exclude) {
                activities.get(i).finish();
            }
        }
    }

    public static void setColdStart(boolean value) {
        coldStart = value;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        foregroundActCount = 0;
    }

    public static int getForegroundActCount() {
        return foregroundActCount;
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }
}
