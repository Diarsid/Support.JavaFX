package diarsid.support.javafx;

import diarsid.support.objects.CommonEnum;

public enum ClickType implements CommonEnum<ClickType> {

    USUAL_CLICK (1000),
    SEQUENTIAL_CLICK (200),
    DOUBLE_CLICK (0);

    private int msAfterLastClick;

    ClickType(int msAfterLastClick) {
        this.msAfterLastClick = msAfterLastClick;
    }

    public int msAfterLastClick() {
        return this.msAfterLastClick;
    }
}
