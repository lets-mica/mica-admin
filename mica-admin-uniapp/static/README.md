# static 目录占位说明

## Tabbar 图标

`static/tabbar/` 下需要 8 个 PNG 文件(每个 81x81 px,推荐 PNG 透明):

```
static/tabbar/
├── workbench.png          # 工作台
├── workbench-active.png
├── message.png            # 消息
├── message-active.png
├── menu.png               # 应用
├── menu-active.png
├── profile.png            # 我的
└── profile-active.png
```

可在 H5 端临时用 emoji 占位,但生产环境建议替换为设计稿。

## 默认头像

`static/default-avatar.png` — 用户未设置头像时的占位图。

## Logo

`static/logo.png` — 登录页 Logo(120x120 px 透明 PNG)。

> **生产前必须替换为真实设计资源**;占位资源仅供开发调试。