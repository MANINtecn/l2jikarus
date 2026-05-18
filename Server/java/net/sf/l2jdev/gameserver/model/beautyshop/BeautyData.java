package net.sf.l2jdev.gameserver.model.beautyshop;

import java.util.HashMap;
import java.util.Map;

public class BeautyData
{
	private final Map<Integer, BeautyItem> _hairList = new HashMap<>();
	private final Map<Integer, BeautyItem> _faceList = new HashMap<>();

	public void addHair(BeautyItem hair)
	{
		this._hairList.put(hair.getId(), hair);
	}

	public void addFace(BeautyItem face)
	{
		this._faceList.put(face.getId(), face);
	}

	public Map<Integer, BeautyItem> getHairList()
	{
		return this._hairList;
	}

	public Map<Integer, BeautyItem> getFaceList()
	{
		return this._faceList;
	}
}
