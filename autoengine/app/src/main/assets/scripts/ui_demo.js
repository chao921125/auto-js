// AI生成
// 简单 UI 示例 —— 演示 ui.layout
"ui";

ui.layout(
    '<vertical padding="16">
        <text text="AutoJs UI 示例" textSize="20sp" textColor="#1976D2" marginBottom="16"/>
        <text id="status" text="就绪" textSize="16sp"/>
        <button id="btnRun" text="运行" marginTop="8"/>
        <button id="btnStop" text="停止" marginTop="4"/>
        <horizontal marginTop="16">
            <text text="进度: " textSize="14sp"/>
            <progressbar id="progress" max="100" progress="0" layout_weight="1"/>
        </horizontal>
    </vertical>'
);

ui.btnRun.on("click", function() {
    ui.status.setText("运行中...");
    for (let i = 0; i <= 100; i += 10) {
        ui.progress.setProgress(i);
        sleep(200);
    }
    ui.status.setText("完成!");
});

ui.btnStop.on("click", function() {
    ui.status.setText("已停止");
    ui.progress.setProgress(0);
});
