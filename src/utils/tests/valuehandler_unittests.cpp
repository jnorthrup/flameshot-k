#include <gtest/gtest.h>
#include "valuehandler.h"
#include "capturetool.h"
#include "capturetoolbutton.h"

using namespace testing;

TEST(ButtonListHelpers, FromIntListAndToIntListRoundtrip) {
    QList<int> ints = {0, 2, 4};
    auto types = ButtonList::fromIntList(ints);
    auto back = ButtonList::toIntList(types);
    EXPECT_EQ(ints.size(), back.size());
    for (int i = 0; i < ints.size(); ++i) {
        EXPECT_EQ(ints[i], back[i]);
    }
}

TEST(NormalizeButtons, RemovesUnknown) {
    QList<int> buttons = {0, 999, 2};
    bool changed = ButtonList::normalizeButtons(buttons);
    EXPECT_TRUE(changed);
    // 999 should be removed
    for (int b : buttons) {
        EXPECT_NE(999, b);
    }
}

TEST(NormalizeButtons, NoChangeWhenAllKnown) {
    auto iterable = CaptureToolButton::getIterableButtonTypes();
    QList<int> buttons = ButtonList::toIntList(iterable);
    bool changed = ButtonList::normalizeButtons(buttons);
    EXPECT_FALSE(changed);
}

int main(int argc, char** argv) {
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
