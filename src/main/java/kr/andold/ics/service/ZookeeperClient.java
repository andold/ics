package kr.andold.ics.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import kr.andold.utils.Utility;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.zookeeper.ZooKeeper;

@Slf4j
@Service
public class ZookeeperClient implements Watcher {
	private static final String ZNODE_PREFIX = "c_";

	@Getter private static boolean isMaster = false;
	@Getter private static String currentZNodeName = "";

	private ZooKeeper zookeeper;

	@Getter private static String userZookeeperConnectString;
	@Value("${user.zookeeper.connect.string}")
	public void setUserZookeeperConnectString(String value) {
		log.info("{} setUserZookeeperConnectString(『{}』)", Utility.indentMiddle(), value);
		userZookeeperConnectString = value;
	}

	@Getter private static String userZookeeperZnodeElectPath;
	@Value("${user.zookeeper.znode.elect.path}")
	public void setUserZookeeperZnodeElectPath(String value) {
		log.info("{} setUserZookeeperZnodeElectPath(『{}』)", Utility.indentMiddle(), value);
		userZookeeperZnodeElectPath = value;
	}

	public void run() {
		log.info("{} run() - 『{}』『{}』", Utility.indentStart(), userZookeeperConnectString, userZookeeperZnodeElectPath);

		try {
			zookeeper = new ZooKeeper(userZookeeperConnectString, 3000, this);
		} catch (IOException e) {
			log.error("IOException:: {}", e.getMessage(), e);
		}

		log.info("{} run() - 『{}』『{}』", Utility.indentEnd(), userZookeeperConnectString, userZookeeperZnodeElectPath);
	}

	@Override
	public void process(WatchedEvent event) {
		log.info("{} process(『{}』) - 『{}:{}』『{}』", Utility.indentStart(), event, event.getType(), event.getState(), currentZNodeName);

		switch(event.getType()) {
		case None:
			switch(event.getState()) {
			case AuthFailed:
				break;
			case Closed:
				try {
					zookeeper = new ZooKeeper(userZookeeperConnectString, 3000, this);
				} catch (IOException e) {
					log.error("IOException:: {}", e.getMessage(), e);
				}
				break;
			case ConnectedReadOnly:
				break;
			case Disconnected:
				updateMaster();
				break;
			case Expired:
				try {
					zookeeper.removeWatches(userZookeeperZnodeElectPath, this, WatcherType.Persistent, true);
					zookeeper.close();
					currentZNodeName = "";
				} catch (InterruptedException e) {
					log.error("InterruptedException:: {}", e.getMessage(), e);
				} catch (KeeperException e) {
					log.error("KeeperException:: {}", e.getMessage(), e);
				}
				break;
			case SaslAuthenticated:
				break;
			case SyncConnected:
				try {
					zookeeper.addWatch(userZookeeperZnodeElectPath, this, AddWatchMode.PERSISTENT);
					zookeeper.create("/elect-ics", new byte[] {}, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					zookeeper.create("/test/elect-ics", new byte[] {}, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					String zNodeFullPath = zookeeper.create(userZookeeperZnodeElectPath + "/" + ZNODE_PREFIX, new byte[] {}, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
					currentZNodeName = zNodeFullPath.replace(userZookeeperZnodeElectPath + "/", "");
				} catch (KeeperException e) {
					log.error("KeeperException:: {}", e.getMessage(), e);
				} catch (InterruptedException e) {
					log.error("InterruptedException:: {}", e.getMessage(), e);
				}
				break;
			default:
				break;
			}
			break;
		case NodeDeleted:
		case NodeCreated:
		case NodeDataChanged:
		case NodeChildrenChanged:
			updateMaster();
			break;
		default:
			updateMaster();
			break;
		}
		
		log.info("{} process(『{}』) - 『{}:{}』『{}』", Utility.indentEnd(), event, event.getType(), event.getState(), currentZNodeName);
	}

	private boolean updateMaster() {
		if (zookeeper == null) {
			return false;
		}

		try {
			List<String> children = zookeeper.getChildren(userZookeeperZnodeElectPath, false);
			Collections.sort(children);
			String smallestChild = children.get(0);
			isMaster = smallestChild.equals(currentZNodeName);
			log.info("{} updateMaster() - {} {} {}", Utility.indentMiddle(), children, currentZNodeName, isMaster);
			return true;
		} catch(Exception e) {
			log.error("Exception:: {}", e.getMessage(), e);
		}

		return false;
	}

}
