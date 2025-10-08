# AdBlock 规则过滤器

这是一个用于合并多个 AdBlock 规则列表的 Java 工具。它可以从多个在线源获取广告拦截规则，并将它们合并为一个统一的规则文件。

## 功能

- 并行下载多个 AdBlock 规则列表
- 去重，避免重复规则
- 只保留有效的广告拦截规则（非注释）

## 使用的规则源

默认包含以下规则源：

1. [Advertising Rules](https://raw.githubusercontent.com/blackmatrix7/ios_rule_script/master/rule/AdGuard/Advertising/Advertising.txt)
2. [adblockdns](https://raw.githubusercontent.com/217heidai/adblockfilters/main/rules/adblockdns.txt)
3. [adblockdnslite](https://raw.githubusercontent.com/217heidai/adblockfilters/main/rules/adblockdnslite.txt)
4. [adblock](https://raw.githubusercontent.com/Cats-Team/AdRules/main/adblock.txt)

