import { NodeClassValue, Step } from "assistsx";
import { useLogStore } from "../stores/logStore";
import { setWechatEnterNext, launchWechat, wechatPackageName } from "./WechatEnter";
import { useNavigationStore } from "../stores/navigationStore";

export const start = () => {
    useLogStore().clearLogs()
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
    const officialAccountNode = step.findById("com.tencent.mm:id/sct", { filterText: "公众号" })[0]
    if (!officialAccountNode) {
        useLogStore().add({ images: [], text: '未找到公众号入口' })
        return undefined
    }
    const clickResult = officialAccountNode.findFirstParentClickable().click();
    if (clickResult) {
        useLogStore().add({ images: [], text: '点击"公众号"' })
    } else {
        useLogStore().add({ images: [], text: '点击"公众号"失败' })
    }
    officialAccountList.length = 0
    return step.next(collectOfficialAccounts)
}

// 导出公众号列表
export const officialAccountList: string[] = []
export const collectOfficialAccounts = async (step: Step): Promise<Step | undefined> => {

    const listNode = step.findById("com.tencent.mm:id/i3y", { filterClass: "android.widget.ListView" })[0]
    const nodes = listNode.getChildren()
    for (let i = 0; i < nodes.length; i++) {
        const child = nodes[i]
        if (child.className != NodeClassValue.LinearLayout) {
            continue
        }
        const name = child.findById("com.tencent.mm:id/awx")[0].text
        useLogStore().add({ images: [], text: `公众号:${name}` })
        if (officialAccountList.includes(name)) {
            continue
        }
        officialAccountList.push(name)
    }
    await step.delay(500)
    const result = listNode.scrollForward()
    if (result) {
        useLogStore().add({ images: [], text: '滚动列表' })
        return step.repeat()
    } else {
        useLogStore().add({ images: [], text: '列表已滚动到底部' })
        // 跳转到取消公众号选择页面
        useNavigationStore().setTargetRoute('/unfollow-official-account')
    }
    return undefined
}