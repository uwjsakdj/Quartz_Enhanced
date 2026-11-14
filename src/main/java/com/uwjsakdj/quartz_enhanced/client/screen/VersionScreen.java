package com.uwjsakdj.quartz_enhanced.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class VersionScreen extends Screen {
    private final Screen parent;
    private UpdateResult updateResult = null;
    private String latestRemoteVersion = null;
    private boolean isChecking = false;

    private static final String MODRINTH_ID = "quartz_enhanced";
    private static final String CURSEFORGE_URL = "https://www.curseforge.com/minecraft/mc-mods/quartz-enhanced";
    private static final String MODRINTH_URL = "https://modrinth.com/mod/quartz_enhanced/versions";
    private static final ResourceLocation QUARTZ_BG = new ResourceLocation("minecraft:textures/block/quartz_block_side.png");

    public VersionScreen(Screen parent) {
        super(Component.translatable("config.quartz_enhanced.version.title"));
        this.parent = parent;

    }

    @Override
    protected void init() {
        // 1. 检查更新按钮
        Component checkText = isChecking ?
                Component.translatable("config.quartz_enhanced.checking") :
                Component.translatable("config.quartz_enhanced.check_update");

        Button checkButton = Button.builder(checkText, (button) -> {
            if (!isChecking) {
                isChecking = true;
                this.rebuildWidgets();
                performUpdateCheck();
            }
        }).bounds(this.width / 2 - 50, this.height / 2 - 60, 100, 20).build();

        checkButton.active = !isChecking;
        this.addRenderableWidget(checkButton);

        // 2. 根据检查结果动态渲染 UI
        int y = this.height / 2 - 20;

        if (updateResult == null) {
            // 初始状态，无额外按钮
        } else if (updateResult == UpdateResult.UP_TO_DATE) {
            this.addRenderableWidget(Button.builder(
                    Component.translatable("config.quartz_enhanced.up_to_date", latestRemoteVersion).withStyle(ChatFormatting.GREEN),
                    (button) -> {}
            ).bounds(this.width / 2 - 80, y, 160, 20).build());

        } else if (updateResult == UpdateResult.NEWER_AVAILABLE) {
            this.addRenderableWidget(Button.builder(
                    Component.translatable("config.quartz_enhanced.new_version", latestRemoteVersion).withStyle(ChatFormatting.YELLOW),
                    (button) -> {}
            ).bounds(this.width / 2 - 80, y - 20, 160, 20).build());

            // CurseForge 下载按钮
            this.addRenderableWidget(Button.builder(
                    Component.translatable("config.quartz_enhanced.download_cf"),
                    (button) -> Util.getPlatform().openUri(CURSEFORGE_URL)
            ).bounds(this.width / 2 - 100, y + 10, 95, 20).build());

            // Modrinth 下载按钮
            this.addRenderableWidget(Button.builder(
                    Component.translatable("config.quartz_enhanced.download_mr"),
                    (button) -> Util.getPlatform().openUri(MODRINTH_URL)
            ).bounds(this.width / 2 + 5, y + 10, 95, 20).build());

        } else if (updateResult == UpdateResult.LOCAL_NEWER) {
            this.addRenderableWidget(Button.builder(
                    Component.translatable("config.quartz_enhanced.error_local_newer").withStyle(ChatFormatting.RED),
                    (button) -> {}
            ).bounds(this.width / 2 - 80, y, 160, 20).build());

        } else if (updateResult == UpdateResult.ERROR_NETWORK) {
            this.addRenderableWidget(Button.builder(
                    Component.translatable("config.quartz_enhanced.error_network").withStyle(ChatFormatting.RED),
                    (button) -> {}
            ).bounds(this.width / 2 - 80, y, 160, 20).build());

        } else if (updateResult == UpdateResult.ERROR_SERVER || updateResult == UpdateResult.ERROR_NOT_FOUND) {
            this.addRenderableWidget(Button.builder(
                    Component.translatable("config.quartz_enhanced.error_server").withStyle(ChatFormatting.RED),
                    (button) -> {}
            ).bounds(this.width / 2 - 80, y, 160, 20).build());
        }

        // 3. 返回按钮 (底部)
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                (button) -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(this.parent);
                    }
                }
        ).bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1. 平铺石英块
        for (int x = 0; x < this.width; x += 16) {
            for (int y = 0; y < this.height; y += 16) {
                guiGraphics.blit(QUARTZ_BG, x, y, 0, 0, 16, 16, 16, 16);
            }
        }
        // 2. 暗色遮罩
        guiGraphics.fill(0, 0, this.width, this.height, 0x99000000);

        // 3. 原本的文字渲染
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        String localVersion = ModList.get().getModContainerById("quartz_enhanced")
                .map(container -> container.getModInfo().getVersion().toString()).orElse("未知");
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("config.quartz_enhanced.current_version", localVersion),
                this.width / 2, 40, 0xAAAAAA);

        // 4. 渲染控件
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // ================== 网络请求与版本比对逻辑 ==================

    private void performUpdateCheck() {
        String apiUrl;
        try {
            // 修复：对 JSON 数组进行 URL 编码，避免 URI 解析异常
            String loaders = java.net.URLEncoder.encode("[\"forge\"]", java.nio.charset.StandardCharsets.UTF_8);
            String gameVersions = java.net.URLEncoder.encode("[\"1.20.1\"]", java.nio.charset.StandardCharsets.UTF_8);
            apiUrl = String.format("https://api.modrinth.com/v2/project/%s/version?loaders=%s&game_versions=%s", MODRINTH_ID, loaders, gameVersions);
        } catch (Exception e) {
            // 如果编码失败，降级为请求所有版本（拿到最新一个再过滤）
            apiUrl = "https://api.modrinth.com/v2/project/" + MODRINTH_ID + "/version";
        }

        String finalApiUrl = apiUrl;
        CompletableFuture.supplyAsync(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(finalApiUrl))
                        .header("User-Agent", "QuartzEnhanced/UpdateChecker (Contact: uwjsakdj)")
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String json = response.body();
                    // 简易解析：拿到第一个 version_number
                    int idx = json.indexOf("\"version_number\":\"");
                    if (idx != -1) {
                        int start = idx + 18;
                        int end = json.indexOf("\"", start);
                        return json.substring(start, end);
                    }
                    return "ERROR_NOT_FOUND";
                } else if (response.statusCode() == 404) {
                    return "ERROR_NOT_FOUND";
                } else {
                    System.err.println("[QuartzEnhanced] Update check failed. HTTP Status: " + response.statusCode());
                    return "ERROR_SERVER";
                }
            } catch (Exception e) {
                // 把具体报错打印到控制台，方便排查
                System.err.println("[QuartzEnhanced] Network request failed!");
                e.printStackTrace();
                return "ERROR_NETWORK";
            }
        }).thenAccept(remoteVersion -> {
            Minecraft.getInstance().execute(() -> {
                String localVersion = ModList.get().getModContainerById("quartz_enhanced")
                        .map(container -> container.getModInfo().getVersion().toString()).orElse("0.0.0");

                this.updateResult = compareVersions(localVersion, remoteVersion);
                this.latestRemoteVersion = remoteVersion;
                this.isChecking = false;
                this.rebuildWidgets();
            });
        });
    }

    private enum UpdateResult {
        UP_TO_DATE, NEWER_AVAILABLE, LOCAL_NEWER, ERROR_NOT_FOUND, ERROR_NETWORK, ERROR_SERVER
    }

    private UpdateResult compareVersions(String local, String remote) {
        if (remote.startsWith("ERROR_")) {
            return UpdateResult.valueOf(remote);
        }

        try {
            // 👈 使用正则提取版本号：只匹配 数字.数字.数字（支持 1.5 或 1.5.3 的格式）
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?");

            java.util.regex.Matcher localMatcher = pattern.matcher(local);
            java.util.regex.Matcher remoteMatcher = pattern.matcher(remote);

            // 如果本地或远程有一方完全没有数字版本号，默认返回已是最新，防止误报
            if (!localMatcher.find() || !remoteMatcher.find()) {
                return UpdateResult.UP_TO_DATE;
            }

            // 提取主版本、次版本、补丁版本
            int lMajor = Integer.parseInt(localMatcher.group(1));
            int lMinor = Integer.parseInt(localMatcher.group(2));
            int lPatch = localMatcher.group(3) != null ? Integer.parseInt(localMatcher.group(3)) : 0;

            int rMajor = Integer.parseInt(remoteMatcher.group(1));
            int rMinor = Integer.parseInt(remoteMatcher.group(2));
            int rPatch = remoteMatcher.group(3) != null ? Integer.parseInt(remoteMatcher.group(3)) : 0;

            // 逐级比对
            if (rMajor > lMajor) return UpdateResult.NEWER_AVAILABLE;
            if (lMajor > rMajor) return UpdateResult.LOCAL_NEWER;

            if (rMinor > lMinor) return UpdateResult.NEWER_AVAILABLE;
            if (lMinor > rMinor) return UpdateResult.LOCAL_NEWER;

            if (rPatch > lPatch) return UpdateResult.NEWER_AVAILABLE;
            if (lPatch > rPatch) return UpdateResult.LOCAL_NEWER;

            return UpdateResult.UP_TO_DATE;

        } catch (Exception e) {
            // 如果解析崩溃，默认认为已是最新，避免红字报错影响玩家体验
            return UpdateResult.UP_TO_DATE;
        }
    }
}