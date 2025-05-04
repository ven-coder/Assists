import { NodeClassValue, Step } from "assistsx";
import { useLogStore } from "../stores/logStore";
import { setWechatEnterNext, launchWechat, wechatPackageName } from "./WechatEnter";

export const start = () => {
    useLogStore().clearLogs()
    setWechatEnterNext(async (step: Step) => {
        return step.next(switchMe, { delayMs: 0 })
    })
    Step.run(launchWechat, { delayMs: 1000 }).then(() => {
        useLogStore().add({ images: [], text: '执行结束' })
    }).catch((error) => {
        useLogStore().add({ images: [], text: '执行失败：' + error })
    })
}

export const switchMe = async (step: Step): Promise<Step | undefined> => {
    const packageName = step.getPackageName();
    if (packageName !== wechatPackageName) {
        useLogStore().add({ images: [], text: '微信打开失败' })
        return undefined
    }

    const bottomBarNode = step.findByTags(NodeClassValue.RelativeLayout, { filterViewId: "com.tencent.mm:id/huj" })[0];
    if (!bottomBarNode) {
        useLogStore().add({ images: [], text: '微信底部栏未找到，尝试返回重试' })
        step.back();
        return step.repeat()
    }

    const meNode = bottomBarNode.findByTags(NodeClassValue.TextView, { filterText: "我", filterViewId: "com.tencent.mm:id/icon_tv", })[0];
    const result = meNode.findFirstParentClickable().click();
    if (result) {
        useLogStore().add({ images: [], text: '点击"我"' })
    } else {
        useLogStore().add({ images: [], text: '点击"我"失败' })
    }
    return step.next(collectAccountInfo)
}

export const collectAccountInfo = async (step: Step): Promise<Step | undefined> => {
    const accountNode = step.findById("com.tencent.mm:id/gxv")[0]

    const nickName = accountNode.findById("com.tencent.mm:id/kbb")[0].text
    useLogStore().add({ images: [], text: "昵称：" + nickName })

    const wechatNo = accountNode.findById("com.tencent.mm:id/ouv")[0].text;
    useLogStore().add({ images: [], text: wechatNo })

    const avatarBase64 = await accountNode.findById("com.tencent.mm:id/a_4")[0].takeScreenshot()
    useLogStore().add({ images: [avatarBase64], text: "头像" })

    return undefined
}