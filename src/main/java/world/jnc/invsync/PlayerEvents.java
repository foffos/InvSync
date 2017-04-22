package world.jnc.invsync;

import java.io.IOException;
import java.util.Optional;
import java.util.zip.DataFormatException;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.Inventory;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import world.jnc.invsync.util.InventorySerializer;
import world.jnc.invsync.util.Pair;

@AllArgsConstructor
public class PlayerEvents implements AutoCloseable {
	private DataSource dataSource;

	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event)
			throws IOException, ClassNotFoundException, DataFormatException {
		loadPlayer(event.getTargetEntity());
	}

	@Listener
	public void onPlayerLeave(ClientConnectionEvent.Disconnect event) throws IOException, DataFormatException {
		savePlayer(event.getTargetEntity());
	}

	public void saveAllPlayers() throws IOException, DataFormatException {
		for (Player player : Sponge.getGame().getServer().getOnlinePlayers()) {
			savePlayer(player);
		}

		InventorySync.getLogger().debug("Saved all player inventories");
	}

	@Override
	public void close() throws IOException, DataFormatException {
		saveAllPlayers();
	}

	private void loadPlayer(@NonNull Player player) throws ClassNotFoundException, IOException, DataFormatException {
		@NonNull
		Inventory inventory = player.getInventory();
		@NonNull
		Inventory enderInventory = player.getEnderChestInventory();

		Optional<Pair<byte[], byte[]>> result = dataSource.loadInventory(player);

		if (result.isPresent()) {
			Pair<byte[], byte[]> resultPair = result.get();

			InventorySerializer.deserializeInventory(resultPair.getLeft(), inventory);
			InventorySerializer.deserializeInventory(resultPair.getRight(), enderInventory);
		}
	}

	private void savePlayer(@NonNull Player player) throws IOException, DataFormatException {
		@NonNull
		Inventory inventory = player.getInventory();
		@NonNull
		Inventory enderInventory = player.getEnderChestInventory();

		dataSource.saveInventory(player, InventorySerializer.serializeInventory(inventory),
				InventorySerializer.serializeInventory(enderInventory));
	}
}
