package com.snowmangame;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= 21) {
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.rgb(235, 247, 253));
            int flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= 26) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            window.getDecorView().setSystemUiVisibility(flags);
        }
        if (Build.VERSION.SDK_INT >= 29) window.setNavigationBarContrastEnforced(false);

        setContentView(new SnowmanView(this));
    }

    static class SnowmanView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Random rnd = new Random();
        private final ArrayList<Dust> dust = new ArrayList<>();

        private final float density;
        private final float textScale;
        private final Vibrator vib;
        private final SharedPreferences prefs;

        private float safeTop = 0f;
        private float safeBottom = 0f;

        // Game state
        private int balls = 0;
        private int score = 0;
        private int bestScore = 0;
        private int qualitySum = 0;
        private boolean face = false;
        private boolean dressed = false;
        private boolean finished = false;
        private String tip = "Проведи пальцем по снігу — куля почне рости";

        // Current snowball rolling / dragging state
        private float rollProgress = 0f;
        private float rollX = Float.NaN;
        private float rollY = Float.NaN;
        private float lastX = 0f;
        private float lastY = 0f;
        private float lastDx = 0f;
        private float lastDy = 0f;
        private float turnPenalty = 0f;
        private boolean rolling = false;
        private boolean draggingBall = false;
        private boolean ballReady = false;

        // Responsive layout geometry
        private final RectF hudRect = new RectF();
        private final RectF tipRect = new RectF();
        private final RectF rollZone = new RectF();
        private final RectF controlsRect = new RectF();
        private final RectF statusBtn = new RectF();
        private final RectF faceBtn = new RectF();
        private final RectF dressBtn = new RectF();
        private final RectF finishBtn = new RectF();
        private final RectF restartBtn = new RectF();

        private float playTop;
        private float playBottom;
        private float baseR, midR, headR;
        private float baseY, midY, headY;
        private float targetX, targetY, targetR;
        private boolean compact = false;
        private boolean narrow = false;

        SnowmanView(Context c) {
            super(c);
            density = getResources().getDisplayMetrics().density;
            float systemScaled = getResources().getDisplayMetrics().scaledDensity;
            // Keep accessibility scaling, but cap it so canvas labels cannot collide on phones.
            textScale = Math.min(systemScaled, density * 1.18f);
            vib = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
            prefs = c.getSharedPreferences("snowman_game", Context.MODE_PRIVATE);
            bestScore = prefs.getInt("best_score", 0);

            text.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(dp(2));
            setFocusable(true);
            setClickable(true);
            setContentDescription("Гра Зліпи сніговика");

            setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                    if (Build.VERSION.SDK_INT >= 30) {
                        Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                        safeTop = bars.top;
                        safeBottom = bars.bottom;
                    } else {
                        safeTop = insets.getSystemWindowInsetTop();
                        safeBottom = insets.getSystemWindowInsetBottom();
                    }
                    invalidate();
                    return insets;
                }
            });
            requestApplyInsets();
        }

        private float dp(float v) { return v * density; }
        private float tx(float v) { return v * textScale; }
        private float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
        private float dist(float x1, float y1, float x2, float y2) {
            return (float) Math.hypot(x1 - x2, y1 - y2);
        }

        private void vibrate(int ms) {
            if (vib == null || !vib.hasVibrator()) return;
            if (Build.VERSION.SDK_INT >= 26) vib.vibrate(VibrationEffect.createOneShot(ms, 85));
            else vib.vibrate(ms);
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);
            layoutGame();
            drawBackground(c);
            drawHud(c);
            drawTip(c);
            drawSnowmanAndTarget(c);
            drawRollZone(c);
            drawDust(c);
            drawControls(c);
            if (finished) drawFinishOverlay(c);
        }

        private void layoutGame() {
            float w = getWidth();
            float h = getHeight();
            float top = safeTop;
            float bottom = h - safeBottom;
            float usableH = Math.max(dp(420), bottom - top);
            compact = usableH < dp(650);
            narrow = w < dp(360);

            float margin = narrow ? dp(10) : dp(14);
            float hudH = compact ? dp(62) : dp(70);
            float tipH = compact ? dp(34) : dp(40);
            float controlsH = compact ? dp(72) : dp(82);

            hudRect.set(margin, top + dp(8), w - margin, top + dp(8) + hudH);
            tipRect.set(margin, hudRect.bottom + dp(7), w - margin, hudRect.bottom + dp(7) + tipH);
            controlsRect.set(dp(4), bottom - controlsH, w - dp(4), bottom);

            playTop = tipRect.bottom + dp(5);
            playBottom = controlsRect.top - dp(6);
            float playH = Math.max(dp(245), playBottom - playTop);
            float rollH = clamp(playH * (compact ? .27f : .29f), dp(86), dp(128));
            rollZone.set(margin, playBottom - rollH, w - margin, playBottom);

            float snowmanBottom = rollZone.top - dp(5);
            float snowmanH = Math.max(dp(155), snowmanBottom - playTop);
            baseR = Math.min(w * .205f, snowmanH / 4.08f);
            baseR = clamp(baseR, dp(35), dp(82));
            midR = baseR * .72f;
            headR = baseR * .54f;

            baseY = snowmanBottom - baseR - dp(3);
            midY = baseY - (baseR + midR) * .84f;
            headY = midY - (midR + headR) * .84f;

            targetX = w / 2f;
            if (balls == 0) { targetY = baseY; targetR = baseR; }
            else if (balls == 1) { targetY = midY; targetR = midR; }
            else { targetY = headY; targetR = headR; }

            if (Float.isNaN(rollX) || Float.isNaN(rollY)) resetRollingBallPosition();
            if (!draggingBall) keepRollingBallInsideZone();
        }

        private void resetRollingBallPosition() {
            rollX = rollZone.centerX();
            rollY = rollZone.centerY() + dp(3);
        }

        private float rollingRadius() {
            if (balls >= 3) return 0f;
            float start = dp(13);
            float max = Math.min(targetR * .72f, rollZone.height() * .36f);
            return start + (max - start) * clamp(rollProgress / 100f, 0f, 1f);
        }

        private void keepRollingBallInsideZone() {
            if (balls >= 3) return;
            float r = Math.max(dp(13), rollingRadius());
            rollX = clamp(rollX, rollZone.left + r + dp(3), rollZone.right - r - dp(3));
            rollY = clamp(rollY, rollZone.top + r + dp(3), rollZone.bottom - r - dp(3));
        }

        private void drawBackground(Canvas c) {
            float w = getWidth();
            float h = getHeight();
            float bottom = h - safeBottom;

            LinearGradient sky = new LinearGradient(0, safeTop, 0, bottom * .64f,
                    Color.rgb(159, 218, 247), Color.rgb(222, 244, 255), Shader.TileMode.CLAMP);
            p.setShader(sky);
            c.drawRect(0, 0, w, h, p);
            p.setShader(null);

            // Distant snow hills.
            p.setColor(Color.argb(218, 255, 255, 255));
            c.drawOval(new RectF(-w * .45f, bottom * .39f, w * .68f, bottom * .68f), p);
            p.setColor(Color.argb(238, 247, 252, 255));
            c.drawOval(new RectF(w * .20f, bottom * .45f, w * 1.35f, bottom * .72f), p);
            p.setColor(Color.rgb(239, 249, 254));
            c.drawRect(0, bottom * .61f, w, h, p);

            // Minimal tree silhouettes to add depth without clutter.
            p.setColor(Color.argb(55, 60, 117, 137));
            for (int i = 0; i < 5; i++) {
                float x = w * (.08f + i * .23f);
                float y = bottom * (.57f + (i % 2) * .018f);
                float s = dp(15 + (i % 3) * 4);
                Path tree = new Path();
                tree.moveTo(x, y - s * 2.3f);
                tree.lineTo(x - s, y);
                tree.lineTo(x + s, y);
                tree.close();
                c.drawPath(tree, p);
            }

            // Static snow flakes.
            p.setColor(Color.argb(150, 255, 255, 255));
            int flakes = compact ? 18 : 26;
            float range = Math.max(dp(120), playBottom - safeTop);
            for (int i = 0; i < flakes; i++) {
                float x = (i * 139f + 31f) % Math.max(1f, w);
                float y = safeTop + dp(16) + ((i * 197f) % range);
                c.drawCircle(x, y, dp(1.1f + (i % 3) * .45f), p);
            }
        }

        private void drawHud(Canvas c) {
            RectF r = hudRect;
            p.setColor(Color.argb(241, 255, 255, 255));
            c.drawRoundRect(r, dp(21), dp(21), p);

            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(Color.rgb(38, 69, 89));
            text.setTextSize(tx(narrow ? 9.5f : 10.5f));
            c.drawText("КУЛЯ " + Math.min(3, balls + 1) + "/3", r.left + dp(16), r.top + dp(20), text);
            text.setTextSize(tx(narrow ? 16 : 18));
            String progressLabel = balls >= 3 ? "ГОТОВО" : (int) rollProgress + "%";
            c.drawText(progressLabel, r.left + dp(16), r.bottom - dp(14), text);

            float barLeft = r.left + (narrow ? dp(80) : dp(92));
            float barRight = r.right - dp(92);
            float barH = dp(9);
            float barTop = r.centerY() - barH / 2f + dp(3);
            RectF bar = new RectF(barLeft, barTop, Math.max(barLeft + dp(20), barRight), barTop + barH);
            p.setColor(Color.rgb(220, 237, 246));
            c.drawRoundRect(bar, barH / 2, barH / 2, p);
            float fillP = balls >= 3 ? 1f : clamp(rollProgress / 100f, 0f, 1f);
            RectF fill = new RectF(bar.left, bar.top, bar.left + bar.width() * fillP, bar.bottom);
            p.setColor(ballReady || balls >= 3 ? Color.rgb(69, 157, 127) : Color.rgb(57, 136, 180));
            if (fill.width() > dp(1)) c.drawRoundRect(fill, barH / 2, barH / 2, p);

            text.setTextAlign(Paint.Align.RIGHT);
            text.setColor(Color.rgb(31, 74, 104));
            text.setTextSize(tx(narrow ? 13 : 15));
            c.drawText("★ " + score, r.right - dp(15), r.top + dp(25), text);
            text.setTextSize(tx(narrow ? 8.5f : 9.5f));
            text.setColor(Color.rgb(107, 133, 149));
            c.drawText("РЕКОРД " + bestScore, r.right - dp(15), r.bottom - dp(14), text);
        }

        private void drawTip(Canvas c) {
            p.setColor(Color.argb(208, 234, 247, 254));
            c.drawRoundRect(tipRect, dp(17), dp(17), p);
            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(tx(narrow ? 10.3f : 11.6f));
            text.setColor(Color.rgb(43, 82, 108));
            c.drawText(tip, tipRect.centerX(), tipRect.centerY() + dp(4), text);
        }

        private void drawSnowmanAndTarget(Canvas c) {
            float cx = getWidth() / 2f;

            // Ground shadow anchors the object visually.
            p.setColor(Color.argb(42, 68, 129, 157));
            c.drawOval(new RectF(cx - baseR * .92f, baseY + baseR * .69f,
                    cx + baseR * .92f, baseY + baseR * 1.05f), p);

            if (balls >= 1) drawBall(c, cx, baseY, baseR, 1);
            if (balls >= 2) drawBall(c, cx, midY, midR, 2);
            if (balls >= 3) drawBall(c, cx, headY, headR, 3);

            if (balls < 3) {
                // Ghost target shows where the completed rolling ball should be placed.
                stroke.setStrokeWidth(dp(2));
                stroke.setPathEffect(new DashPathEffect(new float[]{dp(7), dp(6)}, 0));
                stroke.setColor(Color.argb(ballReady ? 180 : 85, 52, 126, 163));
                c.drawCircle(targetX, targetY, targetR * 1.01f, stroke);
                stroke.setPathEffect(null);

                if (ballReady) {
                    text.setTextAlign(Paint.Align.CENTER);
                    text.setTextSize(tx(narrow ? 9 : 10));
                    text.setColor(Color.rgb(66, 123, 151));
                    c.drawText("ПЕРЕТЯГНИ СЮДИ", targetX, targetY - targetR - dp(8), text);
                }
            }

            if (balls == 0 && !ballReady) {
                text.setTextAlign(Paint.Align.CENTER);
                text.setTextSize(tx(10));
                text.setColor(Color.rgb(104, 153, 178));
                c.drawText("ОСНОВА СНІГОВИКА", cx, targetY, text);
            }

            if (face && balls >= 3) drawFace(c, cx);
            if (dressed && balls >= 3) drawClothes(c, cx);
        }

        private void drawBall(Canvas c, float x, float y, float r, int seed) {
            RadialGradient g = new RadialGradient(x - r * .34f, y - r * .38f, r * 1.42f,
                    new int[]{Color.WHITE, Color.rgb(247, 252, 255), Color.rgb(202, 230, 244)},
                    new float[]{0f, .57f, 1f}, Shader.TileMode.CLAMP);
            p.setShader(g);
            c.drawCircle(x, y, r, p);
            p.setShader(null);

            // Snow texture: subtle compressed patches make balls less plastic-looking.
            p.setColor(Color.argb(28, 101, 161, 190));
            for (int i = 0; i < 7; i++) {
                double a = seed * 1.7 + i * 2.31;
                float px = x + (float) Math.cos(a) * r * (.20f + (i % 3) * .17f);
                float py = y + (float) Math.sin(a) * r * (.17f + (i % 2) * .20f);
                c.drawCircle(px, py, Math.max(dp(1.1f), r * .026f), p);
            }

            p.setColor(Color.argb(95, 255, 255, 255));
            c.drawOval(new RectF(x - r * .52f, y - r * .58f, x - r * .06f, y - r * .25f), p);
            stroke.setColor(Color.argb(72, 106, 165, 194));
            stroke.setStrokeWidth(dp(1));
            c.drawCircle(x, y, r - dp(.5f), stroke);
        }

        private void drawFace(Canvas c, float cx) {
            p.setColor(Color.rgb(38, 50, 60));
            c.drawCircle(cx - headR * .30f, headY - headR * .18f, headR * .075f, p);
            c.drawCircle(cx + headR * .30f, headY - headR * .18f, headR * .075f, p);

            // Tiny eye highlights.
            p.setColor(Color.argb(180, 255, 255, 255));
            c.drawCircle(cx - headR * .325f, headY - headR * .205f, headR * .018f, p);
            c.drawCircle(cx + headR * .275f, headY - headR * .205f, headR * .018f, p);

            Path carrot = new Path();
            carrot.moveTo(cx - headR * .02f, headY + headR * .01f);
            carrot.lineTo(cx + headR * .76f, headY + headR * .10f);
            carrot.lineTo(cx - headR * .02f, headY + headR * .18f);
            carrot.close();
            p.setColor(Color.rgb(241, 116, 31));
            c.drawPath(carrot, p);
            p.setColor(Color.argb(80, 164, 78, 25));
            c.drawLine(cx + headR * .20f, headY + headR * .07f,
                    cx + headR * .30f, headY + headR * .13f, p);

            p.setColor(Color.rgb(48, 62, 71));
            for (int i = -2; i <= 2; i++) {
                float mx = cx + i * headR * .16f;
                float my = headY + headR * .43f + Math.abs(i) * headR * .035f;
                c.drawCircle(mx, my, headR * .043f, p);
            }
            for (int i = 0; i < 3; i++) {
                c.drawCircle(cx, midY - midR * .34f + i * midR * .39f, midR * .052f, p);
            }
        }

        private void drawClothes(Canvas c, float cx) {
            stroke.setColor(Color.rgb(102, 76, 56));
            stroke.setStrokeWidth(Math.max(dp(3), baseR * .036f));
            stroke.setStrokeCap(Paint.Cap.ROUND);
            c.drawLine(cx - midR * .70f, midY - midR * .08f, cx - midR * 1.55f, midY - midR * .54f, stroke);
            c.drawLine(cx + midR * .70f, midY - midR * .08f, cx + midR * 1.55f, midY - midR * .54f, stroke);
            c.drawLine(cx - midR * 1.44f, midY - midR * .48f, cx - midR * 1.70f, midY - midR * .75f, stroke);
            c.drawLine(cx - midR * 1.44f, midY - midR * .48f, cx - midR * 1.72f, midY - midR * .32f, stroke);
            c.drawLine(cx + midR * 1.44f, midY - midR * .48f, cx + midR * 1.70f, midY - midR * .75f, stroke);
            c.drawLine(cx + midR * 1.44f, midY - midR * .48f, cx + midR * 1.72f, midY - midR * .32f, stroke);
            stroke.setStrokeCap(Paint.Cap.BUTT);

            // Hat.
            p.setColor(Color.rgb(42, 58, 72));
            c.drawRoundRect(new RectF(cx - headR * .78f, headY - headR * 1.23f,
                    cx + headR * .78f, headY - headR * 1.04f), dp(5), dp(5), p);
            c.drawRoundRect(new RectF(cx - headR * .50f, headY - headR * 1.85f,
                    cx + headR * .50f, headY - headR * 1.11f), dp(8), dp(8), p);
            p.setColor(Color.rgb(61, 128, 161));
            c.drawRect(cx - headR * .50f, headY - headR * 1.29f,
                    cx + headR * .50f, headY - headR * 1.16f, p);

            // Scarf with a second shadow strip.
            p.setColor(Color.rgb(199, 61, 68));
            c.drawRoundRect(new RectF(cx - midR * .84f, midY - midR * .86f,
                    cx + midR * .84f, midY - midR * .64f), dp(8), dp(8), p);
            c.drawRoundRect(new RectF(cx + midR * .34f, midY - midR * .70f,
                    cx + midR * .61f, midY + midR * .12f), dp(6), dp(6), p);
            p.setColor(Color.argb(55, 85, 22, 30));
            c.drawRect(cx - midR * .76f, midY - midR * .70f,
                    cx + midR * .76f, midY - midR * .65f, p);
        }

        private void drawRollZone(Canvas c) {
            RectF r = rollZone;
            p.setColor(Color.argb(218, 251, 254, 255));
            c.drawRoundRect(r, dp(23), dp(23), p);
            stroke.setStrokeWidth(dp(1.3f));
            stroke.setColor(Color.argb(100, 111, 177, 208));
            c.drawRoundRect(new RectF(r.left + dp(1), r.top + dp(1), r.right - dp(1), r.bottom - dp(1)),
                    dp(22), dp(22), stroke);

            // Subtle rolling tracks.
            p.setColor(Color.argb(45, 84, 153, 184));
            for (int i = 0; i < 5; i++) {
                float yy = r.top + r.height() * (.23f + i * .14f);
                c.drawRoundRect(new RectF(r.left + dp(18), yy, r.right - dp(18), yy + dp(1.2f)),
                        dp(1), dp(1), p);
            }

            if (balls >= 3) {
                text.setTextAlign(Paint.Align.CENTER);
                text.setTextSize(tx(11));
                text.setColor(Color.rgb(75, 139, 164));
                c.drawText("КАРКАС ГОТОВИЙ", r.centerX(), r.centerY() - dp(3), text);
                text.setTextSize(tx(9));
                text.setColor(Color.rgb(126, 163, 181));
                c.drawText("Додай обличчя та одяг", r.centerX(), r.centerY() + dp(16), text);
                return;
            }

            float rr = rollingRadius();
            drawRollingBall(c, rollX, rollY, rr);

            text.setTextAlign(Paint.Align.LEFT);
            text.setTextSize(tx(narrow ? 8.5f : 9.5f));
            text.setColor(Color.rgb(97, 151, 177));
            String label = ballReady ? "ЗАТИСНИ КУЛЮ" : "КОТИ ПАЛЬЦЕМ";
            c.drawText(label, r.left + dp(12), r.top + dp(16), text);
        }

        private void drawRollingBall(Canvas c, float x, float y, float r) {
            RadialGradient g = new RadialGradient(x - r * .32f, y - r * .38f, r * 1.45f,
                    new int[]{Color.WHITE, Color.rgb(244, 251, 255), Color.rgb(190, 224, 240)},
                    new float[]{0f, .56f, 1f}, Shader.TileMode.CLAMP);
            p.setShader(g);
            c.drawCircle(x, y, r, p);
            p.setShader(null);

            stroke.setColor(Color.argb(90, 91, 153, 184));
            stroke.setStrokeWidth(dp(1));
            c.drawCircle(x, y, r - dp(.5f), stroke);

            // Curved compression mark gives a sense that the ball is rolling.
            stroke.setColor(Color.argb(70, 82, 142, 171));
            stroke.setStrokeWidth(Math.max(dp(1), r * .025f));
            RectF arc = new RectF(x - r * .42f, y - r * .42f, x + r * .42f, y + r * .42f);
            c.drawArc(arc, 205, 75, false, stroke);
        }

        private void drawControls(Canvas c) {
            float w = getWidth();
            RectF r = controlsRect;
            p.setColor(Color.argb(205, 248, 253, 255));
            c.drawRoundRect(r, dp(23), dp(23), p);

            float gap = dp(5);
            float outer = dp(7);
            float top = r.top + dp(6);
            float bottom = r.bottom - dp(6);
            float bw = (r.width() - outer * 2 - gap * 3) / 4f;
            float left = r.left + outer;
            statusBtn.set(left, top, left + bw, bottom);
            faceBtn.set(statusBtn.right + gap, top, statusBtn.right + gap + bw, bottom);
            dressBtn.set(faceBtn.right + gap, top, faceBtn.right + gap + bw, bottom);
            finishBtn.set(dressBtn.right + gap, top, r.right - outer, bottom);

            drawButton(c, statusBtn, "КУЛІ", balls + "/3", false, balls >= 3);
            drawButton(c, faceBtn, "ОБЛИЧЧЯ", face ? "ГОТОВО" : "КРОК 2", balls >= 3 && !face, face);
            drawButton(c, dressBtn, "ОДЯГ", dressed ? "ГОТОВО" : "КРОК 3", face && !dressed, dressed);
            drawButton(c, finishBtn, "ФІНІШ", finished ? "ГОТОВО" : "КРОК 4", dressed && !finished, finished);
        }

        private void drawButton(Canvas c, RectF r, String title, String sub, boolean active, boolean done) {
            if (done) p.setColor(Color.rgb(80, 151, 127));
            else if (active) p.setColor(Color.rgb(38, 105, 145));
            else p.setColor(Color.rgb(215, 229, 237));
            c.drawRoundRect(r, dp(16), dp(16), p);

            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(tx(narrow ? 8.2f : 9.7f));
            text.setColor((active || done) ? Color.WHITE : Color.rgb(111, 136, 151));
            c.drawText(title, r.centerX(), r.centerY() - dp(2), text);
            text.setTextSize(tx(narrow ? 6.8f : 7.8f));
            text.setColor((active || done) ? Color.argb(225, 255, 255, 255) : Color.rgb(151, 170, 181));
            c.drawText(sub, r.centerX(), r.centerY() + dp(14), text);
        }

        private void addDust(float x, float y, float distance) {
            if (dust.size() > 36) dust.remove(0);
            float rr = dp(2) + Math.min(dp(4.5f), distance * .018f);
            dust.add(new Dust(x + rnd.nextFloat() * dp(11) - dp(5.5f),
                    y + rnd.nextFloat() * dp(9) - dp(4.5f), rr, 205));
        }

        private void drawDust(Canvas c) {
            Iterator<Dust> it = dust.iterator();
            boolean more = false;
            while (it.hasNext()) {
                Dust d = it.next();
                d.alpha -= 15;
                d.radius += dp(.08f);
                if (d.alpha <= 0) {
                    it.remove();
                    continue;
                }
                more = true;
                p.setColor(Color.argb(d.alpha, 255, 255, 255));
                c.drawCircle(d.x, d.y, d.radius, p);
            }
            if (more) postInvalidateOnAnimation();
        }

        private void drawFinishOverlay(Canvas c) {
            float w = getWidth();
            float top = safeTop;
            float bottom = getHeight() - safeBottom;
            p.setColor(Color.argb(190, 20, 43, 59));
            c.drawRect(0, 0, getWidth(), getHeight(), p);

            float cardW = Math.min(w - dp(30), dp(370));
            float cardH = Math.min(bottom - top - dp(38), compact ? dp(300) : dp(336));
            float left = (w - cardW) / 2f;
            float cardTop = top + (bottom - top - cardH) / 2f;
            RectF card = new RectF(left, cardTop, left + cardW, cardTop + cardH);
            p.setColor(Color.WHITE);
            c.drawRoundRect(card, dp(28), dp(28), p);

            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(Color.rgb(30, 69, 93));
            text.setTextSize(tx(19));
            c.drawText("Сніговик готовий!", card.centerX(), card.top + dp(48), text);

            text.setTextSize(tx(39));
            text.setColor(Color.rgb(39, 117, 159));
            c.drawText(String.valueOf(score), card.centerX(), card.top + dp(109), text);

            int avgQuality = balls == 0 ? 0 : qualitySum / balls;
            text.setTextSize(tx(10));
            text.setColor(Color.rgb(111, 141, 158));
            c.drawText("ТОЧНІСТЬ СКЛАДАННЯ  " + avgQuality + "%", card.centerX(), card.top + dp(139), text);
            c.drawText("РЕКОРД  " + bestScore, card.centerX(), card.top + dp(161), text);

            if (score >= bestScore && score > 0) {
                p.setColor(Color.rgb(230, 246, 238));
                RectF badge = new RectF(card.centerX() - dp(66), card.top + dp(177), card.centerX() + dp(66), card.top + dp(211));
                c.drawRoundRect(badge, dp(15), dp(15), p);
                text.setTextSize(tx(9));
                text.setColor(Color.rgb(57, 132, 105));
                c.drawText("НОВИЙ РЕКОРД", badge.centerX(), badge.centerY() + dp(3), text);
            }

            restartBtn.set(card.left + dp(24), card.bottom - dp(72), card.right - dp(24), card.bottom - dp(20));
            p.setColor(Color.rgb(38, 105, 145));
            c.drawRoundRect(restartBtn, dp(18), dp(18), p);
            text.setTextSize(tx(12));
            text.setColor(Color.WHITE);
            c.drawText("ГРАТИ ЩЕ", restartBtn.centerX(), restartBtn.centerY() + dp(4), text);
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();

            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                lastX = x;
                lastY = y;
                lastDx = 0f;
                lastDy = 0f;

                if (finished) return true;

                if (balls < 3) {
                    float rr = rollingRadius();
                    if (ballReady && dist(x, y, rollX, rollY) <= rr * 1.55f + dp(10)) {
                        draggingBall = true;
                        rolling = false;
                        tip = "Перетягни кулю в пунктирний контур";
                        return true;
                    }
                    if (!ballReady && rollZone.contains(x, y)) {
                        rolling = true;
                        draggingBall = false;
                        rollX = x;
                        rollY = y;
                        keepRollingBallInsideZone();
                        return true;
                    }
                }
                return true;
            }

            if (e.getAction() == MotionEvent.ACTION_MOVE) {
                if (finished) return true;

                if (rolling && !ballReady && balls < 3) {
                    float dx = x - lastX;
                    float dy = y - lastY;
                    float d = (float) Math.hypot(dx, dy);
                    if (d > dp(1.2f)) {
                        float required = dp(355) + targetR * 1.55f;
                        rollProgress = Math.min(100f, rollProgress + (d / required) * 100f);

                        if (lastDx != 0f || lastDy != 0f) {
                            float a = (float) Math.hypot(lastDx, lastDy);
                            float b = Math.max(.001f, d);
                            float cosine = (lastDx * dx + lastDy * dy) / (a * b);
                            if (cosine < -.45f) turnPenalty += 1f;
                        }
                        lastDx = dx;
                        lastDy = dy;

                        rollX = x;
                        rollY = y;
                        keepRollingBallInsideZone();
                        addDust(rollX, rollY, d);

                        if (rollProgress >= 100f) {
                            rollProgress = 100f;
                            ballReady = true;
                            rolling = false;
                            tip = "Куля готова — затисни й перетягни її в контур";
                            vibrate(26);
                        } else {
                            tip = "Коти кулю по снігу: " + (int) rollProgress + "%";
                        }
                        lastX = x;
                        lastY = y;
                        invalidate();
                    }
                    return true;
                }

                if (draggingBall && ballReady && balls < 3) {
                    rollX = clamp(x, dp(8), getWidth() - dp(8));
                    rollY = clamp(y, playTop, playBottom);
                    addDust(rollX, rollY, dp(3));
                    lastX = x;
                    lastY = y;
                    invalidate();
                    return true;
                }

                lastX = x;
                lastY = y;
                return true;
            }

            if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL) {
                performClick();

                if (finished) {
                    if (restartBtn.contains(x, y)) reset();
                    return true;
                }

                if (draggingBall && ballReady && balls < 3) {
                    draggingBall = false;
                    tryPlaceBall();
                    return true;
                }
                rolling = false;

                if (faceBtn.contains(x, y)) addFace();
                else if (dressBtn.contains(x, y)) addDress();
                else if (finishBtn.contains(x, y)) finishGame();
                else if (statusBtn.contains(x, y) && balls < 3) {
                    tip = ballReady ? "Затисни кулю та перетягни в контур" : "Коти кулю пальцем у нижній сніговій зоні";
                    invalidate();
                }
                return true;
            }

            return true;
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }

        private void tryPlaceBall() {
            float d = dist(rollX, rollY, targetX, targetY);
            float threshold = targetR * .90f + rollingRadius() * .30f;
            if (d <= threshold) {
                float accuracy = clamp(1f - d / Math.max(dp(1), threshold), 0f, 1f);
                int quality = Math.round(72 + accuracy * 28 - Math.min(8f, turnPenalty * .45f));
                quality = Math.max(60, Math.min(100, quality));
                qualitySum += quality;
                score += 100 + Math.round(accuracy * 100f);
                balls++;
                vibrate(38);
                rollProgress = 0f;
                ballReady = false;
                turnPenalty = 0f;
                resetRollingBallPosition();

                if (balls < 3) tip = "Точність " + quality + "%. Тепер скоти кулю " + (balls + 1);
                else tip = "Каркас готовий — додай обличчя";
                invalidate();
            } else {
                tip = "Не попав у контур — спробуй поставити точніше";
                resetRollingBallPosition();
                vibrate(14);
                invalidate();
            }
        }

        private void addFace() {
            if (balls < 3 || face) {
                if (balls < 3) tip = "Спочатку склади три снігові кулі";
                invalidate();
                return;
            }
            face = true;
            score += 100;
            tip = "Обличчя готове. Тепер одягни сніговика";
            vibrate(24);
            invalidate();
        }

        private void addDress() {
            if (!face || dressed) {
                if (!face) tip = "Спочатку додай обличчя";
                invalidate();
                return;
            }
            dressed = true;
            score += 100;
            tip = "Шапка, шарф і руки готові — можна завершувати";
            vibrate(24);
            invalidate();
        }

        private void finishGame() {
            if (!dressed || finished) {
                if (!dressed) tip = "Спочатку одягни сніговика";
                invalidate();
                return;
            }
            score += 50;
            finished = true;
            if (score > bestScore) {
                bestScore = score;
                prefs.edit().putInt("best_score", bestScore).apply();
            }
            vibrate(60);
            invalidate();
        }

        private void reset() {
            balls = 0;
            score = 0;
            qualitySum = 0;
            face = false;
            dressed = false;
            finished = false;
            rollProgress = 0f;
            rolling = false;
            draggingBall = false;
            ballReady = false;
            turnPenalty = 0f;
            tip = "Проведи пальцем по снігу — куля почне рости";
            dust.clear();
            resetRollingBallPosition();
            vibrate(18);
            invalidate();
        }

        static class Dust {
            float x, y, radius;
            int alpha;
            Dust(float x, float y, float radius, int alpha) {
                this.x = x;
                this.y = y;
                this.radius = radius;
                this.alpha = alpha;
            }
        }
    }
}
