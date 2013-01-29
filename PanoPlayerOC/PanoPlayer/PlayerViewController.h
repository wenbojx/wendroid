//
//  PlayerViewController.h
//  PanoPlayer
//
//  Created by 李文博 on 13-1-18.
//  Copyright (c) 2013年 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PLView.h"
//#import <SDWebImage/UIImageView+WebCache.h>

@interface PlayerViewController : UIViewController <PLViewDelegate>{
    PLView *plView;
    int faceNum;
    NSMutableArray *hotspots;    
    //PLCubicPanorama *cubicPanorama;
    UIImage *faceSL, *faceSR, *faceSF, *faceSB, *faceSD, *faceSU;
}

//@property(retain, nonatomic)NSString *panoId;
@property(nonatomic, assign)int faceNum;
@property(retain, nonatomic)NSMutableArray *hotspots;
@property(retain, nonatomic)UIImage *faceSL, *faceSR, *faceSF, *faceSB, *faceSD, *faceSU;
//@property(retain, nonatomic)PLCubicPanorama *cubicPanorama;


-(NSString *)getPanoInfoFromUrl:(NSString *)url;
-(void)startPlayer:(NSString *)panoId;
-(void)downLoadImage:(NSString *)url face:(NSString *)face;
-(void)initPlayer:(NSString *)face faceImage:(UIImage *)faceImage;
-(void)displayPano;
-(void)addHotspot:(NSString *)hotspotId linkSceneId:(NSString *)linkSceneId tilt:(NSString *)tilt pan:(NSString *)pan;

@end
