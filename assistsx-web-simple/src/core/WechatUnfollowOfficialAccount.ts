import { NodeClassValue, Step } from "assistsx";
import { useLogStore } from "../stores/logStore";
import { setWechatEnterNext, launchWechat, wechatPackageName } from "./WechatEnter";
import { useNavigationStore } from "../stores/navigationStore";
import { officialAccountList } from "./WechatCollectOfficialAccount";

// 存储要取消关注的公众号列表
let accountsToUnfollow: string[] = [];

export const start = (accounts: string[]) => {
    accountsToUnfollow = [...accounts];
    useLogStore().clearLogs()
    useNavigationStore().setTargetRoute('/logs')
    setWechatEnterNext(async (step: Step) => {
        return step.next(switchContacts, { delayMs: 0 })
    })
    Step.run(launchWechat, { delayMs: 1000 }).then(() => {
        useLogStore().add({ images: [], text: '执行结束' })
    }).catch((error) => {
        useLogStore().add({ images: [], text: '执行失败：' + error })
    })
}

export const switchContacts = async (step: Step): Promise<Step | undefined> => {
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

    const targetNode = bottomBarNode.findByTags(NodeClassValue.TextView, { filterText: "通讯录", filterViewId: "com.tencent.mm:id/icon_tv", })[0];
    const result = targetNode.findFirstParentClickable().click();
    if (result) {
        useLogStore().add({ images: [], text: '点击"通讯录"' })
    } else {
        useLogStore().add({ images: [], text: '点击"通讯录"失败' })
    }
    return step.next(enterOfficialAccount)
}


export const enterOfficialAccount = async (step: Step): Promise<Step | undefined> => {

    const titleContactsNodes = step.findById("android:id/text1")
    for (let i = 0; i < titleContactsNodes.length; i++) {
        const titleContactsNode = titleContactsNodes[i]
        if (titleContactsNode.text === "通讯录") {
            await titleContactsNode.nodeGestureClickByDouble({ clickInterval: 150 })
            break
        }
    }
    await step.delay(1000)
    const clickResult = step.findById("com.tencent.mm:id/sct", { filterText: "公众号" })[0].findFirstParentClickable().click();
    if (clickResult) {
        useLogStore().add({ images: [], text: '点击"公众号"' })
    } else {
        useLogStore().add({ images: [], text: '点击"公众号"失败' })
    }

    return step.next(enterOfficialAccountConversation)
}

export const enterOfficialAccountConversation = async (step: Step): Promise<Step | undefined> => {
    const listNode = step.findById("com.tencent.mm:id/i3y", { filterClass: "android.widget.ListView" })[0]
    const nodes = listNode.getChildren()
    for (let i = 0; i < nodes.length; i++) {
        const child = nodes[i]
        if (child.className != NodeClassValue.LinearLayout) {
            continue
        }
        const name = child.findById("com.tencent.mm:id/awx")[0].text
        if (accountsToUnfollow.includes(name)) {
            await child.nodeGestureClick()
            useLogStore().add({ images: [], text: `点击公众号:${name}` })
            accountsToUnfollow.splice(accountsToUnfollow.indexOf(name), 1)
            return step.next(enterOfficialAccountProfile)
        }
    }
    return undefined
}

export const enterOfficialAccountProfile = async (step: Step): Promise<Step | undefined> => {
    step.findById("com.tencent.mm:id/fq")[0].click()
    useLogStore().add({ images: [], text: '点击"设置"' })
    return step.next(clickUnfollowOfficialAccount)
}
export const clickUnfollowOfficialAccount = async (step: Step): Promise<Step | undefined> => {
    step.findById("com.tencent.mm:id/anv")[0].click()
    useLogStore().add({ images: [], text: '点击"已关注公众号"' })
    return step.next(clickUnfollowOfficialAccountConfirm)
}
export const clickUnfollowOfficialAccountConfirm = async (step: Step): Promise<Step | undefined> => {
    step.findById("com.tencent.mm:id/mm_alert_ok_btn")[0].click()
    useLogStore().add({ images: [], text: '点击"不再关注"' })
    return step.next(checkUnfollowOfficialAccountSuccess)
}
export const checkUnfollowOfficialAccountSuccess = async (step: Step): Promise<Step | undefined> => {

    const waitNode = step.findById("com.tencent.mm:id/jma", { filterText: "请稍候..." })[0]
    if (waitNode) {
        useLogStore().add({ images: [], text: '请稍候...' })
        return step.repeat()
    }
    const followNode = step.findById("com.tencent.mm:id/anv")[0]
    if (!followNode) {
        useLogStore().add({ images: [], text: '已取消关注' })
    }
    return step.next(backOfficialAccountsList)
}

export const backOfficialAccountsList = async (step: Step): Promise<Step | undefined> => {

    const listNode = step.findById("com.tencent.mm:id/i3y", { filterClass: "android.widget.ListView" })[0]
    if (listNode) {
        return step.next(enterOfficialAccountConversation)
    } else {
        step.back()
        return step.repeat()
    }

}