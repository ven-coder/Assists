import { NodeClassValue, Step } from "assistsx";
import { useLogStore } from "../stores/logStore";

export const wechatPackageName = "com.tencent.mm";
let wechatEnterNext: ((step: Step) => Promise<Step | undefined>) | undefined = undefined;

export const setWechatEnterNext = (next: (step: Step) => Promise<Step | undefined>) => {
    wechatEnterNext = next;
}

export const launchWechat = async (step: Step): Promise<Step | undefined> => {
    useLogStore().add({ images: [], text: '开始执行' })
    await step.delay(1000)
    step.launchApp(wechatPackageName);
    useLogStore().add({ images: [], text: '启动微信' })
    return step.next(async (step) => await checkDoubleWechatOpen(step), { delayMs: 1000 })
}

const checkDoubleWechatOpen = async (step: Step): Promise<Step | undefined> => {
    const node = step.findById("com.miui.securitycore:id/app1");
    if (node[0]) {
        node[0].click();
        useLogStore().add({ images: [], text: '微信双开，选择微信1' })
        return step.next(async (step) => await checkMain(step), { delayMs: 1000 })
    }
    return step.next(async (step) => await checkMain(step), { delayMs: 1000 })
}

const checkMain = async (step: Step): Promise<Step | undefined> => {
    const packageName = step.getPackageName();
    if (packageName !== wechatPackageName) {

        if (step.repeatCount > 3) {
            useLogStore().add({ images: [], text: '微信打开失败' })
            return undefined
        }

        return step.repeat()
    }

    const bottomBarNode = step.findByTags(NodeClassValue.RelativeLayout, { filterViewId: "com.tencent.mm:id/huj" })[0];
    if (!bottomBarNode) {
        useLogStore().add({ images: [], text: '微信底部栏未找到，尝试返回重试' })
        step.back();
        return step.repeat()
    }

    if (wechatEnterNext) {
        return step.next(wechatEnterNext)
    }
    return undefined
}

