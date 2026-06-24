# locales

国际化资源(`vue-i18n`),支持中/英切换。

- `zh-CN` — 简体中文
- `en-US` — English

在 `main.ts` 创建 i18n 实例并注入 App。

业务模块通过 `useI18n()` composable 获取:

```ts
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
t('im.conversations') // → '会话' / 'Conversations'
```

如需新增语言,在 `langs/` 下新增文件并在 `index.ts` 注册即可。