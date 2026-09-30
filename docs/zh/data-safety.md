---
layout: default
lang: zh
base: "/zh"
key: "data-safety"
title: 数据安全声明
class: doc
---
# 数据安全声明

<p class="meta">Google Play 管理中心表单（Policy and programs（政策和计划）→ App content（应用内容）→ Data safety（数据安全））的填写答案及每项答案的理由。已于 2026年9月30日 按 2.2.1 版本核对。<a href="{{ page.base }}/privacy">隐私政策</a>是面向用户的、对相同事实的说明。</p>

## 应用如何处理数据

Capital 没有后端。用户输入的所有内容都保存在设备上的一个文件夹中。唯一会离开设备的数据，是应用根据用户的指示，发送给用户在“设置”中选择的第三方数据运营方的内容：钱包公开地址、代币合约 ID、货币代码，以及用户为该运营方填写的 API 密钥（如有）。运营方响应请求；应用将返回的余额和价格保存在本地，不保留请求的任何副本。应用中没有任何 SDK 会向外回传数据：依赖项仅有 AndroidX、Kotlin、OkHttp 和 Bouncy Castle。

根据 Google Play 的规定，只要数据被传输到设备之外，即使不涉及开发者服务器、处理只是临时的，也算作*收集*，因此声明不能填“不收集任何数据”。本应用只声明一种临时处理的、可选的数据类型。

## 表单答案

### 概览

| 问题 | 答案 |
|---|---|
| Does your app collect or share any of the required user data types?（您的应用是否会收集或分享任何必需声明的用户数据类型？） | **Yes（是）** |
| Is all of the user data collected by your app encrypted in transit?（您的应用收集的所有用户数据是否都在传输过程中经过加密？） | **Yes（是）** — 仅使用 HTTPS；清单文件中已禁用明文流量 |
| Do you provide a way for users to request that their data is deleted?（您是否为用户提供了请求删除其数据的方式？） | **Yes（是）** — 请求完成后不保留任何数据，满足该标记所需的“在收集后 90 天内删除”规则。用户可通过删除文件夹并卸载应用来删除设备上的数据；详见隐私政策。 |

### 数据类型

只选择一种类型。

| 类别 | 数据类型 | Collected（收集） | Shared（分享） | Ephemeral（临时处理） | Required or optional（必需或可选） | Purposes（用途） |
|---|---|---|---|---|---|---|
| Financial info（财务信息） | Other financial info（其他财务信息） | Yes（是） | No（否） | **Yes（是）** | **Optional（可选）** | App functionality（应用功能） |

该类型涵盖的内容：用户跟踪的公开区块链地址、在这些地址上发现的代币合约，以及用户持仓的货币代码。这些数据被发送给用户选择的数据运营方，以便获取余额和价格；仅在请求期间保存在内存中，随后即被丢弃。

为什么**不属于分享**：数据由设备直接传给用户选择的运营方，由用户发起的刷新触发；在此之前，应用已在“设置”中告知用户将查询哪个运营方，以及该请求会向该运营方透露地址和 IP。这属于“由用户发起、且用户可合理预期数据会被分享的操作”这一豁免情形。开发者不会收到任何数据，也没有任何服务提供方。

为什么是**可选**：仅使用手动持仓即可完整使用应用。地址和 API 密钥都由用户自愿填写。

用户填写的 API 密钥仅发送给签发该密钥的运营方。它们是用户访问该运营方自身服务的凭据，不作为单独的用户数据类型声明；如果审核人员询问，请按上述内容说明。

### **不**收集的类型

其他所有类别都选“No（否）”：不收集位置信息、个人信息、通讯录、消息、照片或媒体、文件和文档、应用活动、网络浏览记录、应用信息和性能数据（无崩溃日志、无诊断数据）、设备 ID 或其他 ID。IP 地址作为任何 HTTPS 请求的一部分会到达运营方，但应用不会将其用于任何目的。

用户的财务记录（持仓、目标、计划）仅在设备上处理，不在该表单的范围之内。

### 安全做法

| 项目 | 答案 |
|---|---|
| Independent security review（独立安全审核）(MASA) | No（否） |
| Committed to follow the Families policy（承诺遵守“家庭”政策） | No（否）（不是儿童应用） |

## “应用内容”页面上的相关声明

| 声明 | 答案 |
|---|---|
| Privacy policy URL（隐私权政策网址） | `{{ site.url }}/privacy` |
| Ads（广告） | No（否），应用不含广告 |
| App access（应用访问权限） | 所有功能无需特殊访问权限即可使用。无需登录。数据源 API 密钥为可选项；每个数据源都有无需密钥的默认选项。 |
| Content rating（内容分级）(IARC) | 选择实用工具/效率类问卷；无暴力、色情内容、赌博、受管制物品、用户互动或位置分享。预期结果：Everyone / PEGI 3。 |
| Target audience and content（目标受众和内容） | 18 岁及以上（个人理财工具；并非为儿童设计） |
| News app（新闻应用） | No（否） |
| COVID-19 contact tracing and status（COVID-19 接触者追踪和状态） | No（否） |
| Data safety（数据安全） | 如上所述 |
| Government app（政府应用） | No（否） |
| Financial features（金融功能） | 见[金融功能声明]({{ page.base }}/financial-features) |
| Health apps（健康应用） | 无健康功能 |

## 应用变更时需要更新的内容

当某个版本加入了数据分析、崩溃报告、账号、开发者运营的服务器、具有网络访问能力的新 SDK，或在设备上与其他应用共享数据时，请重新核对本页。上述任何一项都会改变表单的答案。
