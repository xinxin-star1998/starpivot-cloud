<script setup lang="ts">
import {onLaunch, onShow} from "@dcloudio/uni-app";
import {refreshCartBadge} from "@/utils/tabbar-cart";

onLaunch(() => {
  refreshCartBadge();
});

onShow(() => {
  refreshCartBadge();
});
</script>
<style lang="scss">
page {
  background-color: $sp-bg-page;
  color: $sp-text;
  font-size: 28rpx;
  line-height: 1.5;
}
</style>
