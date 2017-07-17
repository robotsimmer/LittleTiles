package com.creativemd.littletiles.common.gui.controls;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.CreativeBakedModel;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.gui.GuiRenderHelper;
import com.creativemd.creativecore.gui.client.style.Style;
import com.creativemd.creativecore.gui.container.GuiParent;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.Vec3d;

public class GuiTileViewer extends GuiParent{
	
	public ItemStack stack;
	
	public float scale = 5;
	public float offsetX = 0;
	public float offsetY = 0;
	
	public EnumFacing viewDirection = EnumFacing.EAST;
	
	public boolean visibleAxis = false;
	
	public EnumFacing.Axis normalAxis = null;
	public EnumFacing.Axis axisDirection = EnumFacing.Axis.Y;
	
	public int axisX = 0;
	public int axisY = 0;
	public int axisZ = 0;
	
	public boolean grabbed = false;
	
	public GuiTileViewer(String name, int x, int y, int width, int height, ItemStack stack) {
		super(name, x, y, width, height);
		this.stack = stack;
		this.marginWidth = 0;
		updateNormalAxis();
	}
	
	public void updateNormalAxis()
	{
		List<RenderCubeObject> cubes = ((ICreativeRendered)stack.getItem()).getRenderingCubes(null, null, stack);
		double minX = Integer.MAX_VALUE;
		double minY = Integer.MAX_VALUE;
		double minZ = Integer.MAX_VALUE;
		double maxX = Integer.MIN_VALUE;
		double maxY = Integer.MIN_VALUE;
		double maxZ = Integer.MIN_VALUE;
		
		for (int i = 0; i < cubes.size(); i++) {
			CubeObject cube = cubes.get(i);
			minX = Math.min(minX, cube.minX);
			minY = Math.min(minY, cube.minY);
			minZ = Math.min(minZ, cube.minZ);
			maxX = Math.max(maxX, cube.maxX);
			maxY = Math.max(maxY, cube.maxY);
			maxZ = Math.max(maxZ, cube.maxZ);
		}
		
		double sizeX = maxX-minX;
		double sizeY = maxY-minZ;
		double sizeZ = maxZ-minZ;
		
		switch(axisDirection)
		{
		case X:
			if(sizeY >= sizeZ)
				normalAxis = EnumFacing.Axis.Y;
			else
				normalAxis = EnumFacing.Axis.Z;
			break;
		case Y:
			if(sizeX >= sizeZ)
				normalAxis = EnumFacing.Axis.Z;
			else
				normalAxis = EnumFacing.Axis.X;
			break;
		case Z:
			if(sizeX >= sizeY)
				normalAxis = EnumFacing.Axis.X;
			else
				normalAxis = EnumFacing.Axis.Y;
			break;
		default:
			break;
		}
	}
	
	public void changeNormalAxis()
	{
		switch(axisDirection)
		{
		case X:
			if(normalAxis == EnumFacing.Axis.Z)
				normalAxis = EnumFacing.Axis.Y;
			else
				normalAxis = EnumFacing.Axis.Z;
			break;
		case Y:
			if(normalAxis == EnumFacing.Axis.Z)
				normalAxis = EnumFacing.Axis.X;
			else
				normalAxis = EnumFacing.Axis.Z;
			break;
		case Z:
			if(normalAxis == EnumFacing.Axis.Y)
				normalAxis = EnumFacing.Axis.X;
			else
				normalAxis = EnumFacing.Axis.Y;
			break;
		default:
			break;
		}
	}
	
	public List<BakedQuad> baked = null;
	
	@Override
	protected void renderContent(GuiRenderHelper helper, Style style, int width, int height) {
		
		GlStateManager.pushMatrix();
		
		//Vec3 offset = Vec3.createVectorHelper(p_72443_0_, p_72443_2_, p_72443_4_);
		GL11.glTranslated(this.width/2+offsetX, this.height/2+offsetY, 0);
		GL11.glScaled(4, 4, 4);
		GL11.glScaled(this.scale, this.scale, this.scale);
		GL11.glTranslated(-offsetX*2, -offsetY*2, 0);
		
		GlStateManager.pushMatrix();
		
		if(viewDirection.getAxis() != EnumFacing.Axis.Y)
			GL11.glRotated(180, 0, 0, 1);
		EnumFacing facing = viewDirection;
		switch(viewDirection)
		{
		case EAST:
			GL11.glRotated(180, 0, 1, 0);
			facing = EnumFacing.SOUTH;
			break;
		case WEST:
			//GL11.glRotated(-180, 0, 1, 0);
			facing = EnumFacing.NORTH;
			break;
		case UP:
			GL11.glRotated(-90, 1, 0, 0);
			break;
		case DOWN:
			GL11.glRotated(90, 1, 0, 0);
			break;
		case SOUTH:
			GL11.glRotated(90, 0, 1, 0);
			facing = EnumFacing.EAST;
			break;
		case NORTH:
			GL11.glRotated(-90, 0, 1, 0);
			facing = EnumFacing.WEST;
			break;
		}
		
        if(baked == null)
        {
        	//ItemStack stack = new ItemStack(LittleTiles.multiTiles);
        	//stack.setTagCompound(this.stack.getTagCompound().copy());
	        CreativeBakedModel.setLastItemStack(stack);        
	        
	        baked = new ArrayList<>(CreativeBakedModel.getBlockQuads(null, facing, 0, false));
	        CreativeBakedModel.setLastItemStack(null);
        }
        
        ArrayList<BakedQuad> quads = new ArrayList<>();
        if(visibleAxis)
        {
        	ArrayList<RenderCubeObject> cubes = new ArrayList<>();
        	RenderCubeObject normalCube = new RenderCubeObject(new LittleTileBox(axisX, axisY, axisZ, axisX+1, axisY+1, axisZ+1).getCube(), Blocks.WOOL, 0);
        	normalCube.keepVU = true;
        	float min = -100*1/scale;
        	float max = -min;
        	switch (normalAxis) {
        	case X:
        		normalCube.minX = min;
        		normalCube.maxX = max;
				break;
        	case Y:
        		normalCube.minY = min;
        		normalCube.maxY = max;
				break;
        	case Z:
        		normalCube.minZ = min;
        		normalCube.maxZ = max;
				break;
			default:
				break;
			}
        	cubes.add(normalCube);
        	
        	RenderCubeObject axisCube = new RenderCubeObject(new LittleTileBox(axisX, axisY, axisZ, axisX+1, axisY+1, axisZ+1).getCube(), Blocks.WOOL, 5);
        	cubes.add(axisCube);
        	
        	
        	
        	CreativeBakedModel.getBlockQuads(cubes, quads, (ICreativeRendered) LittleTiles.multiTiles, facing, null, BlockRenderLayer.SOLID, Blocks.WOOL, null, 0, null, false);
        }
        
        helper.renderBakedQuads(baked);
        
        GlStateManager.disableDepth();
        helper.renderBakedQuads(quads);
        GlStateManager.enableDepth();
        
        GlStateManager.popMatrix();
        
        
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
        
        String xAxis = getXFacing().getAxis().name();
        if(getXFacing().getAxisDirection() == AxisDirection.POSITIVE)
        	xAxis += " ->";
        else
        	xAxis = "<- " + xAxis;
        String yAxis = getYFacing().getAxis().name();
        if(getYFacing().getAxisDirection() == AxisDirection.POSITIVE)
        	yAxis += " ->";
        else
        	yAxis = "<- " + yAxis;
        
        /*switch(viewDirection){
        case EAST:
        	xAxis = "X ->";
        	yAxis = "<- Y";
			break;
        case WEST:
        	xAxis = "<- X";
        	yAxis = "<- Y";
			break;
		case DOWN:
			xAxis = "X ->";
	        yAxis = "<- Z";
			break;
		case SOUTH:
			xAxis = "<- Z";
	        yAxis = "<- Y";
			break;
		case NORTH:
			xAxis = "Z ->";
	        yAxis = "<- Y";
			break;
        }*/
        
        helper.drawStringWithShadow(xAxis, 0, 0, width, 14, ColorUtils.WHITE);
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(14, 0, 0);
        GlStateManager.rotate(90, 0, 0, 1);
        helper.drawStringWithShadow(yAxis, 0, 0, width, 14, ColorUtils.WHITE);
        GlStateManager.popMatrix();
	}
	
	public EnumFacing getXFacing()
	{
		switch(viewDirection){
        case EAST:
        	return EnumFacing.EAST;
        case WEST:
        	return EnumFacing.WEST;
        case UP:
        	return EnumFacing.EAST;
		case DOWN:
			return EnumFacing.EAST;
		case SOUTH:
			return EnumFacing.NORTH;
		case NORTH:
			return EnumFacing.SOUTH;
        }
		return EnumFacing.EAST;
	}
	
	public EnumFacing getYFacing()
	{
		switch(viewDirection){
        case EAST:
        	return EnumFacing.DOWN;
        case WEST:
        	return EnumFacing.DOWN;
        case UP:
        	return EnumFacing.SOUTH;
		case DOWN:
			return EnumFacing.NORTH;
		case SOUTH:
			return EnumFacing.DOWN;
		case NORTH:
			return EnumFacing.DOWN;
        }
		return EnumFacing.DOWN;
	}
	
	public EnumFacing getZFacing()
	{
		switch(viewDirection)
		{
		case EAST:
        	return EnumFacing.NORTH;
        case WEST:
        	return EnumFacing.NORTH;
        case UP:
        	return EnumFacing.DOWN;
		case DOWN:
			return EnumFacing.DOWN;
		case SOUTH:
			return EnumFacing.WEST;
		case NORTH:
			return EnumFacing.WEST;
		}
		return EnumFacing.NORTH;
	}
	
	@Override
	public boolean mouseScrolled(int posX, int posY, int scrolled){
		if(scrolled > 0)
			scale *= scrolled*1.5;
		else if(scrolled < 0)
			scale /= scrolled*-1.5;
		return true;
	}
	
	@Override
	public boolean mousePressed(int posX, int posY, int button)
	{
		grabbed = true;
		lastPosition = new Vec3d(posX, posY, 0);
		return true;
	}
	
	public Vec3d lastPosition;
	
	@Override
	public void mouseMove(int posX, int posY, int button){
		//Vec3d mouse = getParent().getMousePos();
		if(grabbed)
		{
			Vec3d currentPosition = new Vec3d(posX, posY, 0);
			if(lastPosition != null)
			{
				Vec3d move = lastPosition.subtract(currentPosition);
				double percent = 0.3;
				offsetX += 1/scale*move.x*percent;
				offsetY += 1/scale*move.y*percent;
			}
			lastPosition = currentPosition;
		}
	}
	
	@Override
	public void mouseReleased(int posX, int posY, int button)
	{
		if(this.grabbed)
		{
			lastPosition = null;
			grabbed = false;
		}
	}
	
	@Override
	public boolean onKeyPressed(char character, int key)
	{
		if(key == Keyboard.KEY_ADD)
		{
			scale *= 2;
			return true;
		}
		if(key == Keyboard.KEY_SUBTRACT)
		{
			scale /= 2;
			return true;
		}
		int ammount = 5;
		if(key == Keyboard.KEY_UP)
		{
			offsetY += ammount;
			return true;
		}
		if(key == Keyboard.KEY_DOWN)
		{
			offsetY -= ammount;
			return true;
		}
		if(key == Keyboard.KEY_RIGHT)
		{
			offsetX -= ammount;
			return true;
		}
		if(key == Keyboard.KEY_LEFT)
		{
			offsetX += ammount;
			return true;
		}
		
		return false;
	}

	public void updateViewDirection() {
		switch(axisDirection)
		{
		case X:
			viewDirection = EnumFacing.SOUTH;
			break;
		case Y:
			viewDirection = EnumFacing.UP;
			break;
		case Z:
			viewDirection = EnumFacing.EAST;
			break;
		default:
			break;
		}
		updateNormalAxis();
		baked = null;
	}
}
