package avatar.message;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import avatar.constants.NpcName;
import avatar.handler.NpcHandler;
import avatar.item.Item;
import avatar.model.Menu;
import avatar.model.Npc;
import avatar.network.Message;
import avatar.network.Session;
import avatar.service.FarmService;
import avatar.constants.Cmd;

public class FarmMsgHandler extends MessageHandler {

    private FarmService service;

    public FarmMsgHandler(Session client) {
        super(client);
        this.service = new FarmService(client);
    }

    @Override
    public void onMessage(Message mss) {
        if (mss == null) {
            return;
        }
        if (this.client.user == null) {
            return;
        }
        try {
            System.out.println("FarmMsgHandler: " + mss.getCommand());
            switch (mss.getCommand()) {
                case Cmd.SET_BIG_FARM: {
                    this.service.setBigFarm(mss);
                    break;
                }
                case Cmd.BUY_ITEM: {
                    this.service.Buy_item_farm(mss);
                    break;
                }
                case Cmd.BUY_ANIMAL: {
                    this.service.Buy_ANIMAL(mss);
                    break;
                }
                case Cmd.PRICE_ANIMAL: {
                    this.service.priceAnimal(mss);
                    break;
                }
                case Cmd.SELL_ANIMAL: {
                    this.service.sellAnimal(mss);
                    break;
                }
                case Cmd.UPDATE_LAND: {
                    this.service.doRequestslot2(mss,this.client.user);
                    break;
                }
                case Cmd.GET_BIG_FARM: {
                    this.service.getBigFarm(mss);
                    break;
                }
                case Cmd.GET_IMAGE_FARM: {
                    this.service.getImageData();
                    break;
                }
                case Cmd.GET_TREE_INFO: {
                    this.service.getTreeInfo(mss);
                    break;
                }
                case Cmd.HARVEST_ANIMAL: {
                    this.service.harvestAnimal(mss);
                    break;
                }
                case Cmd.INVENTORY: {
                    this.service.getInventory(mss);
                    break;
                }
                case Cmd.JOIN: {
                    this.service.joinFarm(mss);
                    break;
                }
                case Cmd.GET_IMG_FARM: {
                    this.service.getImgFarm(mss);
                    break;
                }
                case Cmd.REQUEST_SLOT: {
                    this.service.doRequestslot(mss,this.client.user);
                    break;
                }
                case Cmd.TREE_HARVEST: {
                    this.service.treeHarvest(mss);
                    break;
                }

                case Cmd.OPEN_LAND: {
                    this.service.openLand(mss,this.client.user);
                    break;
                }

                case Cmd.PLANT_SEED: {
                    this.service.plandSeed(mss);
                    break;
                }
                case Cmd.HARVEST_STARFRUIT: {
                    this.service.harvestStarFruil(mss);
                    break;
                }
                case Cmd.UPDATE_STARFRUIL: {
                    this.service.updateStarFruil(mss);
                    break;
                }
                case Cmd.REQUEST_FRIENDLIST: {
                    this.client.requestFriendList(mss);
                    break;
                }
                case Cmd.GET_INFO_STARFRUIT: {
                    this.service.InforStarfruil(this.client.user);
                    break;
                }
                case Cmd.REQUEST_CHARGE_MONEY_INFO: {
                    this.service.Infor(this.client.user);
                    break;
                }
                case Cmd.GET_CARD: {
                    this.service.sellFarmitm(this.client.user, mss);
                    break;
                }
                case Cmd.UPDATE_FARM_CATTLE: {
                    this.service.updateFarmCattle(mss);
                    break;
                }
                case Cmd.UPDATE_FARM_FISH: {
                    this.service.updateFarmFish(mss);
                    break;
                }
                case Cmd.COOKING: {
                    this.service.Cooking(mss);
                    break;
                }
                case Cmd.HARVEST_COOK: {
                    this.service.harvestCook(mss);
                    break;
                }
                case Cmd.NAU_NHANH: {
                    this.service.fastCooking(mss);
                    break;
                }
                case Cmd.COMMUNICATE: {
                    if (this.client.user != null) {
                            this.client.doCommunicate(mss);
                            break;
                        }
                        break;
                }
                default:
                    super.onMessage(mss);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}