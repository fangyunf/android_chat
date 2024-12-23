// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.contact;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.contactkit.ui.databinding.ShopNewFragmentBinding;
import com.netease.yunxin.kit.contactkit.ui.normal.contact.adapter.ShoprListAdapter;
import com.yaoxin.appbase.fragment.BaseFragment;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.view.CommonGridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * contact page
 */
public class ShopNewFragment extends BaseFragment {
    private final String TAG = "ContactFragment";
    private ShopNewFragmentBinding binding;
    ShoprListAdapter adapter = new ShoprListAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ShopNewFragmentBinding.inflate(inflater, container, false);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        binding.shopNewFragmentRv.setLayoutManager(gridLayoutManager);
        CommonGridSpacingItemDecoration gridSpacingItemDecoration =
                new CommonGridSpacingItemDecoration(2, SizeUtils.dp2px(10), false);
        binding.shopNewFragmentRv.addItemDecoration(gridSpacingItemDecoration);
        List<GroupInfoBean> data = new ArrayList<>();
        // 图片URL数组
String[] imageUrls = {
    "https://img2.baidu.com/it/u=2441920201,1057085013&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=533",
    "https://img1.baidu.com/it/u=2450120045,1777628418&fm=253&fmt=auto&app=138&f=JPEG?w=696&h=500",
    "http://t13.baidu.com/it/u=3969801106,555309175&fm=224&app=112&f=JPEG?w=500&h=500",
    "https://m.360buyimg.com/mobilecms/s750x750_jfs/t6019/323/2026519602/83998/de825fee/593a084fN31d2eed0.jpg%21q80.jpg",
    "http://t15.baidu.com/it/u=3909023002,3275374159&fm=224&app=112&f=JPEG?w=500&h=333",
    "https://img01.yzcdn.cn/upload_files/2020/07/20/FtL-2PVjUn2fLm2oLtIk_J-lSnkl.png%21middle.jpg",
    "https://pics3.baidu.com/feed/14ce36d3d539b600f2ac7825c9e8042dc75cb705.jpeg?token=e1898181ab92cc860fb7a0d96c75807d",
    "https://m.360buyimg.com/mobilecms/s750x750_jfs/t16717/313/940311777/218543/7a1cde0a/5ab1f06aN63d61d78.jpg%21q80.jpg",
    "https://img01.yzcdn.cn/upload_files/2019/06/19/Fhz_PeJiLkR_Tsc-XE9Z2HkiwbLk.jpg%21middle.jpg",
    "https://m.360buyimg.com/mobilecms/s750x750_jfs/t12478/133/1721840369/262610/3c81ba2c/5a2667f7N0efbb528.jpg%21q80.jpg",
    "https://img14.360buyimg.com/pop/jfs/t1/59735/19/11330/116002/5d8c271eEe8b5f06b/7ecb68a18c402aec.jpg",
    "https://img01.yzcdn.cn/upload_files/2020/06/12/Fukv-kRW8xo5HVosWHpIJJS0Z4fY.jpg%21middle.jpg",
    "https://img01.yzcdn.cn/upload_files/2021/05/15/FhyJFynd47ZY4Mbl4zRsT_fsIIWK.jpg%21middle.jpg",
    "https://a.vpimg2.com/upload/merchandise/pdcvis/2017/03/02/197/164cd802295243578e8b86e99862ea84-651.jpg",
    "https://source.shop.busionline.com/201612011934-storeid-21393-5641de44a7d285641de44a7d711447157316.jpg",
    "https://wx4.sinaimg.cn/mw690/0063nsE9ly1huhnesw5r1j60m80m8q6002.jpg",
    "https://img.yzcdn.cn/upload_files/2020/10/22/FuJUdUeEbBIbke9XOoQerfElO5lX.jpg%21middle.jpg",
    "https://ww4.sinaimg.cn/mw690/006P4QiMgy1hua58sthskj30p00xcjtv.jpg",
    "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.alicdn.com%2Fbao%2Fuploaded%2FTB1QXqRVSrqK1RjSZK9XXXyypXa.png&refer=http%3A%2F%2Fimg.alicdn.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1732435706&t=3e76df727478cb45380a6c70de7ea2c6",
    "https://www.ourqm.com/upload/2021/04/20/20210420212150885.jpg",
    "https://wx4.sinaimg.cn/mw690/0063nsE9ly1huhnesw5r1j60m80m8q6002.jpg",
    "https://img.yzcdn.cn/upload_files/2020/10/22/FuJUdUeEbBIbke9XOoQerfElO5lX.jpg%21middle.jpg",
    "https://ww4.sinaimg.cn/mw690/006P4QiMgy1hua58sthskj30p00xcjtv.jpg",
    "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.alicdn.com%2Fbao%2Fuploaded%2FTB1QXqRVSrqK1RjSZK9XXXyypXa.png&refer=http%3A%2F%2Fimg.alicdn.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1732435706&t=3e76df727478cb45380a6c70de7ea2c6",
    "https://www.ourqm.com/upload/2021/04/20/20210420212150885.jpg"
};

// 商品名称数组
String[] names = {
    "T恤",
    "玩偶猫",
    "哈卡曼顿智能音响",
    "劳特莱男手表",
    "俄罗斯进口酒",
    "纯棉家用面巾",
    "荣事达空气炸锅",
    "美味饼干",
    "尖头平底小皮鞋",
    "儿童雨靴",
    "苏泊尔保温壶",
    "滋源柔顺洗发水",
    "海尔洗衣机",
    "口红",
    "德芙巧克力",
    "吉列剃须刀",
    "皮棉拖鞋女",
    "时尚连衣裙女",
    "薄款冰丝袜子",
    "GUCCI女士香水",
    "吉列剃须刀",
    "皮棉拖鞋女",
    "时尚连衣裙女",
    "薄款冰丝袜子",
    "GUCCI女士香水"
};

// 价格数组
String[] prices = {
    "¥99",
    "¥128",
    "¥399",
    "¥6899",
    "¥188",
    "¥18",
    "¥166",
    "¥68",
    "¥198",
    "¥38",
    "¥188",
    "¥98",
    "¥2688",
    "¥158",
    "¥288",
    "¥78",
    "¥28",
    "¥488",
    "¥48",
    "¥429",
    "¥78",
    "¥28",
    "¥488",
    "¥48",
    "¥429"
};
        for (int i = 0; i < prices.length; i++) {
            GroupInfoBean groupInfoBean = new GroupInfoBean();
            groupInfoBean.name = names[i];
            groupInfoBean.price1 = prices[i];
            groupInfoBean.avatar = imageUrls[i];
            data.add(groupInfoBean);
        }
        adapter.setItems(data);
        binding.shopNewFragmentRv.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<GroupInfoBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                Map map = new HashMap();
                map.put("data", new Gson().toJson(adapter.getItem(i)));
                SubmitOrderActivity.start(SubmitOrderActivity.class, requireActivity(), map);
            }
        });
        return binding.getRoot();
    }



    @Override
    public void onPause() {
        super.onPause();
    }

}
