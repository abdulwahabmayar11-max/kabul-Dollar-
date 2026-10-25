package com.kabulpay.app;

import android.app.*;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private final int RED = Color.rgb(166, 8, 27);
    private final int BG = Color.rgb(246, 247, 250);
    private SharedPreferences prefs;
    private LinearLayout root, body, nav;
    private String lang = "fa";
    private String page = "home";
    private String username = "";
    private boolean loggedIn = false;
    private int balance = 5000;
    private ArrayList<String> history = new ArrayList<>();

    private String tr(String fa, String ps, String en) {
        if ("ps".equals(lang)) return ps;
        if ("en".equals(lang)) return en;
        return fa;
    }
    private TextView label(String value, int size, boolean bold, int color) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setPadding(8, 9, 8, 9);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        return t;
    }
    private Button button(String text, boolean primary) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(primary ? Color.WHITE : RED);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primary ? RED : Color.rgb(255,235,238)));
        return b;
    }
    private EditText input(String hint, int type) {
        EditText e = new EditText(this);
        e.setSingleLine(true); e.setHint(hint); e.setInputType(type);
        return e;
    }
    private void addSpace(LinearLayout parent, int h) {
        View v = new View(this);
        parent.addView(v, new LinearLayout.LayoutParams(1, h));
    }
    private void save() {
        prefs.edit().putInt("balance", balance).putString("username", username)
          .putBoolean("loggedIn", loggedIn).putString("lang", lang)
          .putString("history", joinHistory()).apply();
    }
    private String joinHistory() {
        StringBuilder b = new StringBuilder();
        for (String s : history) b.append(s.replace("\n"," ")).append("\n");
        return b.toString();
    }
    private void load() {
        balance = prefs.getInt("balance", 5000);
        username = prefs.getString("username", "");
        loggedIn = prefs.getBoolean("loggedIn", false);
        lang = prefs.getString("lang", "fa");
        String h = prefs.getString("history", "");
        if (!h.isEmpty()) history.addAll(Arrays.asList(h.split("\n")));
    }
    private void tx(String text, int delta) {
        balance += delta;
        String stamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date());
        history.add(0, stamp + " · " + text + " · " + (delta >= 0 ? "+" : "") + delta + " AFN");
        if (history.size() > 100) history.remove(history.size()-1);
        save(); render();
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("KabulPayLocalDemo", MODE_PRIVATE);
        load();
        getWindow().setStatusBarColor(Color.rgb(94,0,10));
        getWindow().setNavigationBarColor(Color.BLACK);
        showLoginOrApp();
    }
    private void showLoginOrApp() {
        if (!loggedIn) renderLogin(); else render();
    }
    private void setup(String title) {
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG);
        TextView brand = label("◉  کابل پی | Kabul Pay", 22, true, Color.WHITE);
        brand.setBackgroundColor(RED); brand.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(brand, new LinearLayout.LayoutParams(-1, 64));
        TextView subtitle = label(title, 18, true, RED);
        subtitle.setPadding(16, 12, 16, 8); root.addView(subtitle);
        ScrollView scroll = new ScrollView(this);
        body = new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(16, 4, 16, 14);
        scroll.addView(body); root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }
    private void renderLogin() {
        setup(tr("ورود / ثبت‌نام", "ننوتل / ثبت نام", "Login / Sign up"));
        body.addView(label(tr("به کابل پی خوش آمدید", "کابل پی ته ښه راغلاست", "Welcome to Kabul Pay"), 21, true, RED));
        body.addView(label(tr("این نسخه آزمایشی است. رمز شما فقط روی همین موبایل ذخیره می‌شود.", "دا ازمایښتي نسخه ده. پټنوم یوازې په همدې موبایل کې ساتل کېږي.", "Demo version. Your password stays on this device only."), 13, false, Color.DKGRAY));
        EditText name = input(tr("نام کاربر", "د کارن نوم", "User name"), 1);
        EditText password = input(tr("رمز آزمایشی (حداقل 4 حرف)", "ازمایښتي پټنوم (لږ تر لږه ۴ توري)", "Demo password (4+ chars)"), 129);
        body.addView(name); body.addView(password); addSpace(body, 8);
        body.addView(button(tr("ورود / ایجاد حساب آزمایشی", "ننوتل / ازمایښتي حساب جوړول", "Sign in / Create demo account"), true));
        Button enter = (Button) body.getChildAt(body.getChildCount()-1);
        enter.setOnClickListener(v -> {
            String n = name.getText().toString().trim(), p = password.getText().toString();
            if (n.length() < 2 || p.length() < 4) {
                toast(tr("نام و رمز معتبر وارد کنید.", "سم نوم او پټنوم ولیکئ.", "Enter a valid name and password."));
                return;
            }
            // This is a local demo profile, not secure server authentication.
            username = n; loggedIn = true; save(); render();
        });
        addSpace(body, 8);
        languageButtons();
        body.addView(label(tr("انتقال پول واقعی غیرفعال است؛ از رمز بانکی یا کد تأیید واقعی استفاده نکنید.", "د اصلي پیسو لېږد بند دی؛ د بانک رمز یا اصلي تایید کوډ مه کاروئ.", "Real-money transfers are disabled. Never enter banking passwords or real verification codes."), 12, false, RED));
    }
    private void languageButtons() {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
        for (String[] item : new String[][]{{"دری","fa"},{"پښتو","ps"},{"English","en"}}) {
            Button b = button(item[0], lang.equals(item[1]));
            row.addView(b, new LinearLayout.LayoutParams(0, -2, 1));
            b.setOnClickListener(v -> { lang = item[1]; save(); showLoginOrApp(); });
        }
        body.addView(row);
    }
    private void render() {
        setup(tr("کیف پول من", "زما بټوه", "My wallet"));
        LinearLayout wallet = new LinearLayout(this); wallet.setOrientation(LinearLayout.VERTICAL);
        wallet.setPadding(16, 12, 16, 14); wallet.setBackgroundColor(Color.WHITE);
        body.addView(wallet);
        wallet.addView(label(tr("موجودی آزمایشی", "ازمایښتي بیلانس", "Demo balance"), 14, false, Color.DKGRAY));
        wallet.addView(label(String.format(Locale.US, "%,d AFN", balance), 30, true, RED));
        wallet.addView(label(tr("کاربر: ", "کارن: ", "User: ") + username, 13, false, Color.DKGRAY));
        wallet.addView(label(tr("نسخهٔ آزمایشی · پول واقعی نیست", "ازمایښتي نسخه · اصلي پیسې نه دي", "Demo only · not real money"), 12, true, RED));
        addSpace(body, 8);
        languageButtons();
        addSpace(body, 8);
        addNavButton(tr("صفحهٔ اصلی", "کور", "Home"), "home");
        addNavButton(tr("ارسال / دریافت آزمایشی", "ازمایښتي لېږل / ترلاسه کول", "Demo transfer"), "transfer");
        addNavButton(tr("تاریخچه", "تاریخچه", "History"), "history");
        addNavButton(tr("پروفایل", "پروفایل", "Profile"), "profile");
        addNavButton(tr("امنیت", "امنیت", "Security"), "security");
        addNavButton(tr("مدیریت (نمایشی)", "مدیریت (ازمایښتي)", "Admin (demo)"), "admin");
        addSpace(body, 8);
        if ("transfer".equals(page)) renderTransfer();
        else if ("history".equals(page)) renderHistory();
        else if ("profile".equals(page)) renderProfile();
        else if ("security".equals(page)) renderSecurity();
        else if ("admin".equals(page)) renderAdmin();
        else {
            body.addView(label(tr("دسترسی سریع", "چټک لاسرسی", "Quick actions"), 18, true, RED));
            TextView note = label(tr("از این نسخه برای آزمایش رابط کاربری استفاده کنید. انتقال واقعی هنوز فعال نیست.", "دا نسخه د انترفېس د ازمایلو لپاره ده. اصلي لېږد فعال نه دی.", "Use this build to test the interface. Real transfers are not enabled."), 14, false, Color.DKGRAY);
            body.addView(note);
        }
        Button logout = button(tr("خروج از حساب", "له حسابه وتل", "Log out"), false);
        body.addView(logout); logout.setOnClickListener(v -> { loggedIn=false; save(); renderLogin(); });
    }
    private void addNavButton(String text, String target) {
        Button b = button(text, page.equals(target));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2); lp.bottomMargin = 4;
        body.addView(b, lp);
        b.setOnClickListener(v -> { page = target; render(); });
    }
    private void renderTransfer() {
        body.addView(label(tr("انتقال آزمایشی", "ازمایښتي لېږد", "Demo transfer"), 18, true, RED));
        body.addView(label(tr("هیچ پول واقعی ارسال نمی‌شود. فقط موجودی نمایشی تغییر می‌کند.", "اصلي پیسې نه لېږل کېږي. یوازې ازمایښتي بیلانس بدلېږي.", "No real money is sent. Only the demo balance changes."), 12, false, RED));
        EditText recipient = input(tr("نام گیرنده", "د ترلاسه کوونکي نوم", "Recipient name"), 1);
        EditText amount = input(tr("مبلغ به افغانی", "مبلغ په افغانیو", "Amount in AFN"), 2);
        body.addView(recipient); body.addView(amount);
        Button send = button(tr("ثبت ارسال آزمایشی", "ازمایښتي لېږد ثبتول", "Record demo transfer"), true); body.addView(send);
        send.setOnClickListener(v -> {
            String n = recipient.getText().toString().trim(); int a;
            try { a = Integer.parseInt(amount.getText().toString()); } catch(Exception e) { toast(tr("مبلغ درست وارد کنید.", "سم مبلغ ولیکئ.", "Enter a valid amount.")); return; }
            if(n.isEmpty() || a < 1 || a > balance) { toast(tr("نام یا مبلغ درست نیست.", "نوم یا مبلغ سم نه دی.", "Invalid name or amount.")); return; }
            tx(tr("ارسال آزمایشی به ", "ازمایښتي لېږد: ", "Demo send to ") + n, -a);
            toast(tr("فقط در نسخهٔ آزمایشی ثبت شد.", "یوازې په ازمایښتي نسخه کې ثبت شو.", "Recorded in demo only."));
        });
        Button receive = button(tr("ثبت دریافت آزمایشی", "ازمایښتي ترلاسه کول", "Record demo receipt"), false); body.addView(receive);
        receive.setOnClickListener(v -> {
            String n = recipient.getText().toString().trim(); int a;
            try { a = Integer.parseInt(amount.getText().toString()); } catch(Exception e) { toast(tr("مبلغ درست وارد کنید.", "سم مبلغ ولیکئ.", "Enter a valid amount.")); return; }
            if(a < 1 || a > 1000000) { toast(tr("مبلغ درست نیست.", "مبلغ سم نه دی.", "Invalid amount.")); return; }
            tx(tr("دریافت آزمایشی از ", "ازمایښتي ترلاسه کول له ", "Demo receipt from ") + (n.isEmpty() ? "—" : n), a);
        });
    }
    private void renderHistory() {
        body.addView(label(tr("تاریخچهٔ تراکنش‌ها", "د لېږدونو تاریخچه", "Transaction history"), 18, true, RED));
        if(history.isEmpty()) body.addView(label(tr("هنوز تراکنشی وجود ندارد.", "تر اوسه لېږد نشته.", "No transactions yet."), 14, false, Color.DKGRAY));
        else for(String item: history) body.addView(label(item, 13, false, Color.DKGRAY));
    }
    private void renderProfile() {
        body.addView(label(tr("پروفایل کاربر", "د کارن پروفایل", "User profile"), 18, true, RED));
        EditText n = input(tr("نام نمایشی", "ښکاره نوم", "Display name"), 1); n.setText(username); body.addView(n);
        Button saveProfile = button(tr("ذخیرهٔ پروفایل", "پروفایل ساتل", "Save profile"), true); body.addView(saveProfile);
        saveProfile.setOnClickListener(v -> { String s=n.getText().toString().trim(); if(s.length()<2){toast(tr("نام کوتاه است.", "نوم ډېر لنډ دی.", "Name is too short."));return;} username=s; save(); toast(tr("ذخیره شد.", "وساتل شو.", "Saved.")); render(); });
    }
    private void renderSecurity() {
        body.addView(label(tr("امنیت و حریم خصوصی", "امنیت او محرمیت", "Security & privacy"), 18, true, RED));
        body.addView(label(tr("• ورود فعلی فقط محلی و آزمایشی است.\n• رمز در سرور امن ذخیره نمی‌شود.\n• احراز هویت، رمز یک‌بارمصرف و قفل دستگاه در این نمونه پیاده‌سازی نشده است.\n• برای استفادهٔ واقعی به backend امن و بازبینی امنیتی نیاز است.", "• ننوتل یوازې ازمایښتي او محلي دی.\n• پټنوم په خوندي سرور کې نه ساتل کېږي.\n• د اصلي کارونې لپاره خوندي backend او امنیتي ارزونه اړینه ده.", "• Sign-in is local demo only.\n• No password is stored on a server.\n• OTP and device lock are not implemented.\n• Production use requires a secure backend and security review."), 14, false, Color.DKGRAY));
        Button clear = button(tr("پاک‌کردن اطلاعات این موبایل", "د دې موبایل معلومات پاکول", "Clear local demo data"), false); body.addView(clear);
        clear.setOnClickListener(v -> new AlertDialog.Builder(this).setMessage(tr("همهٔ موجودی و تاریخچهٔ آزمایشی پاک شود؟", "ټول ازمایښتي بیلانس او تاریخچه پاکه شي؟", "Clear all demo balance and history?")).setNegativeButton(tr("لغو","لغوه","Cancel"),null).setPositiveButton(tr("پاک کن","پاکول","Clear"),(d,w)->{prefs.edit().clear().apply();balance=5000;history.clear();username="";loggedIn=false;lang="fa";renderLogin();}).show());
    }
    private void renderAdmin() {
        body.addView(label(tr("بخش مدیریت — نمایشی", "د مدیریت برخه — ازمایښتي", "Admin panel — demo"), 18, true, RED));
        body.addView(label(tr("این بخش مدیریت واقعی نیست و هیچ حساب یا تراکنش واقعی را اداره نمی‌کند.", "دا اصلي مدیریت نه دی او اصلي حسابونه نه اداره کوي.", "This is not a real admin system and cannot manage real accounts."), 13, false, Color.DKGRAY));
        body.addView(label(tr("تعداد فعالیت‌های محلی: ", "د محلي فعالیتونو شمېر: ", "Local activity count: ") + history.size(), 16, true, RED));
        body.addView(label(tr("موجودی نمایشی فعلی: ", "اوسنی ازمایښتي بیلانس: ", "Current demo balance: ") + balance + " AFN", 16, true, RED));
    }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }
}
