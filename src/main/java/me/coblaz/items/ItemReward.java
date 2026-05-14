package me.coblaz.items;

import javax.annotation.Nonnull;

public record ItemReward(
        @Nonnull String itemId,
        int quantity
) {}