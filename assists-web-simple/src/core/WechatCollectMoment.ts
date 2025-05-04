import { Node, NodeClassValue, Step } from "assistsx";
import { useLogStore } from "../stores/logStore";
import { setWechatEnterNext, launchWechat, wechatPackageName } from "./WechatEnter";

export const start = () => {
    useLogStore().clearLogs()
    setWechatEnterNext(async (step: Step) => {
        return step.next(switchDiscover)
    })
    Step.run(launchWechat, { delayMs: 1000 }).then(() => {
        useLogStore().add({ images: [], text: '执行结束' })
    }).catch((error) => {
        useLogStore().add({ images: [], text: '执行失败：' + error })
    })
}

const switchDiscover = async (step: Step): Promise<Step | undefined> => {
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

    const meNode = bottomBarNode.findByTags(NodeClassValue.TextView, { filterText: "发现", filterViewId: "com.tencent.mm:id/icon_tv", })[0];
    const result = meNode.findFirstParentClickable().click();
    if (result) {
        useLogStore().add({ images: [], text: '点击"发现"' })
    } else {
        useLogStore().add({ images: [], text: '点击"发现"失败' })
    }
    return step.next(enterMoment)
}

export const enterMoment = async (step: Step): Promise<Step | undefined> => {

    const result = step.findById("com.tencent.mm:id/m7k")[0].click();
    if (result) {
        useLogStore().add({ images: [], text: '点击"朋友圈"' })
    } else {
        useLogStore().add({ images: [], text: '点击"朋友圈"失败' })
    }

    return step.next(collectMoment)
}
export const collectMoment = async (step: Step): Promise<Step | undefined> => {
    const listNode = step.findById("com.tencent.mm:id/hbs")[0]

    const children = listNode.getChildren()

    for (let i = 0; i < children.length; i++) {
        const child = children[i]
        if (child.className === NodeClassValue.LinearLayout) {
            const nicknameNode = child.findById("com.tencent.mm:id/kbq")[0]

            const nickname = nicknameNode.text
            useLogStore().add({ images: [], text: "昵称：" + nickname })

            const nodes = child.getNodes()
            const imageNodes: Node[] = []
            let text: string = ""
            for (let j = 0; j < nodes.length; j++) {
                const node = nodes[j]
                if (node.className == NodeClassValue.TextView) {
                    text = text + "\n" + node.text
                }
                if (node.className == NodeClassValue.ImageView) {
                    imageNodes.push(node)
                }
                if (node.className == NodeClassValue.View && node.des.startsWith("图片")) {
                    imageNodes.push(node)
                }
            }

            const images = await step.takeScreenshotNodes(imageNodes)

            useLogStore().add({ images: images, text: text })

            await step.delay(1000)
        }
    }

    if (step.repeatCount < 3) {
        listNode.scrollForward()
        useLogStore().add({ images: [], text: '翻页' })
        return step.repeat()
    }
    return undefined
}