package com.yaoxin.appbase.net;



import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.ParamsBean;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.RequestParams1Bean;
import com.yaoxin.appbase.model.RequestParamsBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.ServiceBase;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by will
 * on 2018/11/14.
 */
public interface ServiceW {

    @POST("/bindCard/createUptadeZFB")
    Call<NetData> bindCard_createUptadeZFB2(
            @Body RequestParams1Bean userBean
    );
    @POST("pass/user/v1/login")
    Call<NetData<UserBean>> login(
            @Body UserBean userBean
    );
    @POST("/customer/smsCode")
    Call<NetData> customer_smsCode(
            @Body RegisterBean userBean
    );
    @POST("/customer/register")
    Call<NetData> customer_register(
            @Body RegisterBean userBean
    );
    @POST("/customer/login")
    Call<NetData> customer_login(
            @Body RegisterBean userBean
    );
    @POST("/customer/updatePassword")
    Call<NetData> customer_updatePassword(
            @Body RegisterBean userBean
    );
    @POST("/friends/search")
    Call<NetData> friends_search(
            @Body RegisterBean userBean
    );
    @POST("/friends/addFriends")
    Call<NetData> friends_addFriends(
            @Body RegisterBean userBean
    );
    @POST("/friends/applyList")
    Call<NetData> friends_applyList(
            @Body RegisterBean userBean
    );
    @POST("/friends/appFriendApplyEd")
    Call<NetData> friends_appFriendApplyEd(
            @Body RegisterBean userBean
    );
    @POST("/friends/updateRemark")
    Call<NetData> friends_updateRemark(
            @Body RegisterBean userBean
    );
    @POST("/friends/searchByUserIdF")
    Call<NetData> friends_searchByUserIdF(
            @Body RegisterBean userBean
    );
    @POST("/groupMember/queryGroupMemberBanneds")
    Call<NetData> groupMember_queryGroupMemberBanneds(
            @Body RegisterBean userBean
    );
    @POST("/red/sendGroupRedpacket")
    Call<NetData> red_sendGroupRedpacket(
            @Body RegisterBean userBean
    );
    @POST("/red/personRedpacket")
    Call<NetData> red_personRedpacket(
            @Body RegisterBean userBean
    );
    @POST("/red/sendExclusiveRedPacket")
    Call<NetData> red_sendExclusiveRedPacket(
            @Body RegisterBean userBean
    );
    @POST("/home/updateFullPassword")
    Call<NetData> home_updateFullPassword(
            @Body RegisterBean userBean
    );
    @POST("/home/getUserByToken")
    Call<NetData> home_getUserByToken(
            @Body RegisterBean userBean
    );
    @POST("/consumer/certify")
    Call<NetData> consumer_certify(
            @Body RegisterBean userBean
    );
    @POST("/customer/ydCodeCheck")
    Call<NetData> customer_ydCodeCheck(
            @Body RegisterBean userBean
    );
    @POST("/consumer/certified")
    Call<NetData> consumer_certified();
    @POST("/red/redpacketDetail")
    Call<NetData> red_redpacketDetail(
            @Body RegisterBean userBean
    );
    @GET("/home/balance")
    Call<NetData> home_balance();
    @POST("/bindCard/bindingCards")
    Call<NetData> bindCard_bindingCards();
    @GET("/home/myQrCode")
    Call<NetData> home_myQrCode();
    @POST("/home/logout")
    Call<NetData> home_logout();

    @POST("/red/recivePersonRedpacket")
    Call<NetData> red_recivePersonRedpacket(
            @Body RegisterBean userBean
    );
    @POST("/red/reciveExclusiveRedpacket")
    Call<NetData> red_reciveExclusiveRedpacket(
            @Body RegisterBean userBean
    );
    @POST("/red/grab")
    Call<NetData> red_grab(
            @Body RegisterBean userBean
    );
    @POST("/red/checkRedpacet")
    Call<NetData> red_checkRedpacet(
            @Body RegisterBean userBean
    );
    @POST("/red/sendRecord")
    Call<NetData> red_sendRecord(
            @Body RegisterBean userBean
    );
    @POST("/red/reciveRecord")
    Call<NetData> red_reciveRecord(
            @Body RegisterBean userBean
    );
    @POST("/red/transcationsList")
    Call<NetData> red_transcationsList(
            @Body RegisterBean userBean
    );
    @POST("/group/createGroup")
    Call<NetData> group_createGroup(
            @Body RegisterBean userBean
    );
    @POST("/group/dissolveGroup")
    Call<NetData> group_dissolveGroup(
            @Body RegisterBean userBean
    );
    @POST("/group/quitGroup")
    Call<NetData> group_quitGroup(
            @Body RegisterBean userBean
    );
    @POST("/home/changeInfo")
    Call<NetData> home_changeInfo(
            @Body RegisterBean userBean
    );
    @POST("/group/groupUserListPost")
    Call<NetData> group_groupUserListPost(
            @Body RegisterBean userBean
    );

    @Multipart
    @POST("/customer/upload")
    Call<NetData> customer_upload(
            @Part MultipartBody.Part image, @Part("description") RequestBody description
    );


    @GET("/group/groupUserList")
    Call<NetData> group_groupUserList(
            @Query("groupId") String groupId
    );

    //群首页信息
    @GET("/group/groupHomeInfo")
    Call<NetData> group_groupHomeInfo(
            @Query("groupId") String groupId
    );

    //群管理
    @GET("/group/groupManage")
    Call<NetData> group_groupManage(
            @Query("groupId") String groupId
    );

    //群管理列表
    @GET("/group/groupAdminList")
    Call<NetData> group_groupAdminList(
            @Body RegisterBean userBean
    );

    //群管理列表
    @GET("/home/securityPrivacy")
    Call<NetData> home_securityPrivacy();


    //  修改群头像昵称
    @POST("/group/updateGroupInfo")
    Call<NetData> group_updateGroupInfo(
            @Body RegisterBean userBean
    );

    //  修改群头像昵称
    @POST("/group/userGroups")
    Call<NetData> group_userGroups(
            @Body RegisterBean userBean
    );
    //  修改群头像昵称
    @POST("/aideNews/scroll")
    Call<NetData> aideNews_scroll(
            @Body RegisterBean userBean
    );

    //  修改群头像昵称
    @POST("/customer/systemAppUser")
    Call<NetData> customer_systemAppUser(
            @Body RegisterBean userBean
    );
    //   转让群主
    @POST("/group/transferGroup")
    Call<NetData> group_transferGroup(
            @Body RegisterBean userBean
    );
    //   我的副号
    @POST("/home/wdfh")
    Call<NetData> home_wdfh();
    //   我的副号
    @POST("/meteor/list")
    Call<NetData> meteor_list();
    //   我的副号
    @POST("/home/gmfh")
    Call<NetData> home_gmfh(
            @Body RegisterBean userBean
    );
    @POST("/meteor/buyMember")
    Call<NetData> meteor_buyMember(
            @Body RegisterBean userBean
    );
    //   群升级
    @POST("/group/buyGroupGrade")
    Call<NetData> group_buyGroupGrade(
            @Body RegisterBean userBean
    );
    //   转让群主
    @GET("/customer/versionCkeck")
    Call<NetData> customer_versionCkeck(
                        @Query("type") String type ,
                        @Query("version") String version
    );
    //   转让群主
    @GET("/customer/noticeList")
    Call<NetData> customer_noticeList();
    //   转让群主
    @POST("/home/changeSecurityPrivacy")
    Call<NetData> home_changeSecurityPrivacy(
            @Body RegisterBean userBean
    );
    // 邀请人入群
    @POST("/group/pullPeopleGroup")
    Call<NetData> group_pullPeopleGroup(
            @Body RegisterBean userBean
    );
    // 踢出群成员
    @POST("/group/outGroup")
    Call<NetData> group_outGroup(
            @Body RegisterBean userBean
    );
    // 踢出群成员
    @POST("groupMember/installGroupNickName")
    Call<NetData> groupMember_installGroupNickName(
            @Body RegisterBean userBean
    );
    // 踢出群成员
    @POST("groupMember/invitationGroupConfirmed")
    Call<NetData> groupMember_invitationGroupConfirmed(
            @Body RegisterBean userBean
    );
    // 设置管理员
    @POST("/group/installAdmin")
    Call<NetData> group_installAdmin(
            @Body RegisterBean userBean
    );
    // 设置单个成员禁抢
    @POST("/groupMember/invitationGroupBanOnLooting")
    Call<NetData> groupMember_invitationGroupBanOnLooting(
            @Body RegisterBean userBean
    );
    //  群成员信息（群点击群成员）
    @POST("/groupMember/groupMemberInfo")
    Call<NetData> groupMember_groupMemberInfo(
            @Body RegisterBean userBean
    );
    //  好友列表
    @POST("/friends/friendList")
    Call<NetData> friends_friendList(
            @Body RegisterBean userBean
    );
    //  好友列表
    @POST("/friends/applyListNum")
    Call<NetData> friends_applyListNum(
            @Body RegisterBean userBean
    );
    //  好友列表
    @POST("/group/groupConsentOrRefuse")
    Call<NetData> group_groupConsentOrRefuse(
            @Body RegisterBean userBean
    );
    //  好友列表
    @POST("/group/applyGroups")
    Call<NetData> group_applyGroups(
            @Body RegisterBean userBean
    );
    //  好友列表
    @POST("/friends/delFriend")
    Call<NetData> friends_delFriend(
            @Body RegisterBean userBean
    );
    //  好友列表
    @POST("/pay/jhzs")
    Call<NetData> pay_jhzs(
            @Body RequestParamsBean userBean
    );
    //  好友列表
    @POST("/pay/six")
    Call<NetData> pay_six(
            @Body RequestParamsBean userBean
    );
    //  好友列表
    @POST("/pay/xxPay")
    Call<NetData> pay_xxPay(
            @Body RequestParamsBean userBean
    );
    //  好友列表
    @POST("/pay/sixL")
    Call<NetData> pay_sixL(
            @Body RequestParamsBean userBean
    );
    //  好友列表
    @POST("/pay/syPay")
    Call<NetData> pay_syPay(
            @Body RequestParamsBean userBean
    );
    //  好友列表
    @POST("/pay/tyPay")
    Call<NetData> pay_tyPay(
            @Body RequestParamsBean userBean
    );
    //  好友列表
    @POST("/customer/about")
    Call<NetData> customer_about(
            @Body RegisterBean userBean
    );
    //  好友列表
    @POST("/bindCard/userZFB")
    Call<NetData> bindCard_userZFB(
            @Body RegisterBean userBean
    );
    //  好友列表
    @POST("/withdraw/withdrawDeposit")
    Call<NetData> withdraw_withdrawDeposit(
            @Body RegisterBean userBean
    );
    //  好友列表
    @POST("/bindCard/createUptadeZFB")
    Call<NetData> bindCard_createUptadeZFB(
            @Body RegisterBean userBean
    );

    //  好友列表
    @POST("/bindCard/createUptadeZFB")
    Call<NetData> bindCard_createUptadeZFB1(
            @Body RequestParamsBean userBean
    );

    //  好友列表
    @POST("/pay/createCardApply")
    Call<NetData> pay_createCardApply(
            @Body RequestParamsBean userBean
    );
    //  好友列表
    @POST("/pay/adaPay")
    Call<NetData> pay_adaPay(
            @Body ParamsBean userBean
    );
    //  好友列表
    @POST("/pay/balancePay")
    Call<NetData> pay_balancepay(
            @Body ParamsBean userBean
    );
    //  好友列表
    @POST("/pay/createCardConfirm")
    Call<NetData> pay_createCardconfirm(
            @Body RequestParamsBean userBean
    );
    //  好友列表
    @POST("/card/buildCard")
    Call<NetData> card_buildCard(
            @Body RequestParamsBean userBean
    );
    //  好友列表
    @POST("/pay/adaPayConfirm")
    Call<NetData> pay_adaPayConfirm(
            @Body RequestParamsBean userBean
    );
    //  好友列表
    @POST("/aideNews/aideMsg")
    Call<NetData> aideNews_aideMsg(
            @Body RegisterBean userBean
    );
    //  好友列表
    @POST("/friends/changeBlackState")
    Call<NetData> friends_changeBlackState(
            @Body RegisterBean userBean
    );
    //  好友列表
    @POST("/friends/blackList")
    Call<NetData> friends_blackList(
            @Body RegisterBean userBean
    );
    //  好友列表
    @POST("/group/groupBlackList")
    Call<NetData> group_groupBlackList(
            @Body RegisterBean userBean
    );
    //  好友列表
    @GET("/customer/notice")
    Call<NetData> customer_notice();
    //  好友列表
    @GET("/home/selectSoundSwith")
    Call<NetData> home_selectSoundSwith();


    //  好友列表
    @POST("/home/soundSwitch")
    Call<NetData> home_soundSwitch(
            @Body RegisterBean userBean
    );
    //  好友列表
    @POST("/group/groupGrade")
    Call<NetData> group_groupGrade();
    //  群拉黑
    @POST("/group/addDeleteBlack")
    Call<NetData> group_addDeleteBlack(
            @Body RegisterBean userBean
    );



    //  彩蛋列表
    @POST("/caidan/caidanList")
    Call<NetData> caidan_caidanList(
            @Body RegisterBean userBean
    );
    //  购买彩蛋  caiDanId
    @POST("/caidan/gmCaidan")
    Call<NetData> caidan_gmCaidan(
            @Body RegisterBean userBean
    );
    //  我的彩蛋
    @POST("/caidan/wdCaidan")
    Call<NetData> caidan_wdCaidan(
            @Body RegisterBean userBean
    );
    //  我的彩蛋
    @POST("/caidan/yffCaidan")
    Call<NetData> caidan_yffCaidan(
            @Body RegisterBean userBean
    );
    //  我的彩蛋  caiDanId //彩蛋id groupId//群id
    @POST("/caidan/sjCaiDan")
    Call<NetData> caidan_sjCaiDan(
            @Body RegisterBean userBean
    );
    //   购买会员价格
    @POST("/caidan/huiYuanJia")
    Call<NetData> caidan_huiYuanJia(
            @Body RegisterBean userBean
    );
    //   购买会员
    @POST("/caidan/gmHuiYuan")
    Call<NetData> caidan_gmHuiYuan(
            @Body RegisterBean userBean
    );
    //   购买会员 入参groupId
    @POST("/caidan/groupCaidan")
    Call<NetData> caidan_groupCaidan(
            @Body RegisterBean userBean
    );
    //   购买会员 入参groupId
    @POST("/caidan/caidaning")
    Call<NetData> caidan_caidaning(
            @Body RegisterBean userBean
    );

}
