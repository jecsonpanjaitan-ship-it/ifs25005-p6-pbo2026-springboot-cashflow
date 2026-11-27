package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25005springbootcashflow.domain.entity.BalanceSummary;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

/** Kartu ringkasan: total transaksi, pemasukan, pengeluaran, dan saldo. */
public class SummaryCards extends Div {

    private final FinancePresenter presenter;
    private final Span count = new Span();
    private final Span income = new Span();
    private final Span expense = new Span();
    private final Span balance = new Span();

    public SummaryCards(FinancePresenter presenter) {
        this.presenter = presenter;
        addClassName("summary-cards");
        add(card("Total Transaksi", count, "stat-neutral"),
                card("Pemasukan", income, "stat-income"),
                card("Pengeluaran", expense, "stat-expense"),
                card("Saldo", balance, "stat-balance"));
        update(new BalanceSummary(0, 0, 0));
    }

    public void update(BalanceSummary summary) {
        count.setText(String.valueOf(summary.count()));
        income.setText(presenter.formatAmount(summary.totalIncome()));
        expense.setText(presenter.formatAmount(summary.totalExpense()));
        balance.setText(presenter.formatAmount(summary.balance()));
    }

    private static Div card(String label, Span value, String cssClass) {
        Span title = new Span(label);
        title.addClassName("stat-label");
        value.addClassName("stat-value");
        Div card = new Div(title, value);
        card.addClassNames("stat-card", cssClass);
        return card;
    }
}
