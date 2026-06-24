# modules/extension

二次开发预留目录,**只新增不修改**。

通用模块(`auth/profile/contacts/...`)不要改动。

每个二次开发模块建议结构:

```
extension/
├── approval/            # 审批(占位)
│   ├── pages/
│   │   ├── index.vue
│   │   ├── apply.vue
│   │   └── detail.vue
│   ├── stores/
│   └── api.ts
├── attendance/          # 考勤(占位)
└── ...
```

二次开发流程:
1. 后端新增表 + 服务层 + Controller
2. App 在 `extension/{模块名}/` 下写实现
3. 在 `pages.json` 注册路由
4. 替换占位 UI

详细说明见 [docs/app/extension.md](../../../../docs/app/extension.md)。