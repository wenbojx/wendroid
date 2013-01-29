//
//  MapViewController.h
//  PanoPlayer
//
//  Created by 李文博 on 13-1-18.
//  Copyright (c) 2013年 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MTImageMapView.h"
@interface MapViewController : UIViewController
<MTImageMapDelegate>

//@property (retain, nonatomic) NSString *mapUrl;
@property(nonatomic, retain) NSArray *coordsData;
@property(nonatomic, retain) NSMutableArray *linkScene;

@property(nonatomic, retain)NSString *responseData;
@property (nonatomic, assign) IBOutlet UIScrollView         *viewScrollStub;
@property (nonatomic, assign) IBOutlet MTImageMapView       *viewImageMap;
@property (nonatomic, retain)          NSArray              *stateNames;

-(NSString *)getPanoMapFromUrl:(NSString *)url;
-(void)displayMap:(UIImage *)mapImage;
-(void)downLoadImage:(NSString *)url;
-(UIImage *)addImageLogo:(UIImage *)img waterMark:(UIImage *)logo left:(float)left top:(float)top;

@end
