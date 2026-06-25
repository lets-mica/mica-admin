# static 目录占位说明

## Tabbar 图标(三选一)

### 方案 1:emoji 嵌在 text(当前默认,零依赖)

`pages.json` 已配置 `text: "🏠 工作台"` 这种 emoji + 文字 同行模式。
emoji 在 H5 / iOS / Android 三端基本一致显示,但 **emoji 与文字同行**(不分行),
适合快速调试或不愿维护图标的阶段。

### 方案 2:纯文字(无图标)

```json
{
  "pagePath": "pages/index/index",
  "text": "工作台"
}
```

### 方案 3:iconPath + selectedIconPath(图标 + 文字双行,最接近原设计)

在 `static/tabbar/` 下放 8 个 81x81 PNG(普通+选中),然后在 `pages.json` 还原字段:

```
static/tabbar/
├── workbench.png          # 工作台(普通)
├── workbench-active.png   # 工作台(选中)
├── message.png            # 消息(普通)
├── message-active.png     # 消息(选中)
├── menu.png               # 应用(普通)
├── menu-active.png        # 应用(选中)
├── profile.png            # 我的(普通)
└── profile-active.png     # 我的(选中)
```

推荐配色:
- 普通 `#7A7E83`(灰)
- 选中 `#18A37E`(绿,与 `pages.json` 的 `selectedColor` 一致)

可在 [iconfont.cn](https://www.iconfont.cn) 或 [flaticon.com](https://www.flaticon.com) 下载,PNG 导出尺寸 81x81,透明背景。

## 默认头像(可选)

`static/default-avatar.png` — 用户未设置头像时的占位图。

## Logo(可选)

`static/logo.png` — 登录页 Logo(120x120 px 透明 PNG)。

> **生产前建议替换为真实设计资源**;占位资源仅供开发调试。