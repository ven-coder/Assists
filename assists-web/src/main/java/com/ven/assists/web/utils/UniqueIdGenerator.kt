package com.ven.assists.web.utils

import java.util.UUID

object UniqueIdGenerator {
    /**
     * 生成标准 UUID 字符串
     * @return 形如 "550e8400-e29b-41d4-a716-446655440000" 的唯一字符串
     */
    fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * 生成不带横杠的 UUID
     * @return 形如 "550e8400e29b41d4a716446655440000" 的唯一字符串
     */
    fun generateUUIDWithoutDashes(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    /**
     * 生成基于 UUID 和时间戳的唯一 ID
     * @return 形如 "550e8400-e29b-41d4-a716-446655440000-1712061234567" 的唯一字符串
     */
    fun generateUUIDWithTimestamp(): String {
        return "${UUID.randomUUID()}-${System.currentTimeMillis()}"
    }
}